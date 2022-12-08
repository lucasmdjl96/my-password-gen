/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.kotest

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.server.kotest.dto.*
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.tables.Users
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and

class UserTest : FunSpec({
    val testTransaction = initDatabase()

    context("create user") {
        context("with no cookie") {
            should("respond with Unauthorized" and "not create any user") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        UserServerDto.arb()
                    ) { fullDatabaseDto, userServerDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.post(UserRoute.Register()) {
                            setBody(userServerDto)
                            contentType(ContentType.Application.Json)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with Unauthorized" and "not create any user") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        UserServerDto.arb(),
                        Arb.uuid()
                    ) { fullDatabaseDto, userServerDto, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.post(UserRoute.Register()) {
                            setBody(userServerDto)
                            contentType(ContentType.Application.Json)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("with not a currently existing user from session") {
                should("respond with OK" and "respond with empty list of email ids" and "create the user on the session" and "set the user as last user") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            UserServerDto.arb()
                        ) { fullDatabaseDto, userServerDto ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                fullSession.userSet.isNotEmpty() &&
                                        !fullSession.userSet.map { it.username }
                                            .contains(userServerDto.username)
                            } ?: return@checkAll
                            val usersBefore = testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                                User.find { Users.sessionId eq session.id }.count()
                            }
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.post(UserRoute.Register()) {
                                setBody(userServerDto)
                                contentType(ContentType.Application.Json)
                            }
                            response shouldHaveStatus HttpStatusCode.OK
                            val responseBody = response.body<UserClientDto>()
                            responseBody.shouldNotBeNull()
                            responseBody.emailIdSet.shouldBeEmpty()
                            testTransaction {
                                User.find { Users.sessionId eq session.id and (Users.username eq userServerDto.username.encode()) }
                                    .shouldNotBeEmpty()
                                val session1 = Session.findById(session.id)
                                session1.shouldNotBeNull()
                                session1.lastUser.shouldNotBeNull()
                                session1.lastUser!!.username shouldBe userServerDto.username.encode()
                                session1.lastUser!!.session.id.value shouldBe session.id
                                User.find { Users.sessionId eq session.id }
                                    .count() shouldBe usersBefore + 1
                                for (fullSessionDto in fullDatabaseDto.sessionSet.filterNot { it.id == session.id }) {
                                    FullSessionDto.recoverFromDatabase(fullSessionDto.id) shouldBe fullSessionDto.encode()
                                }
                                for (fullUserDto in session.userSet.filterNot { it.username == userServerDto.username.encode() }) {
                                    FullUserDto.recoverFromDatabase(fullUserDto.id) shouldBe fullUserDto.encode()
                                }
                            }
                        }
                    }
                }
            }
            context("with a currently existing user from session") {
                should("respond with Conflict" and "last user should be null" and "not create any user") {
                    testApplication {
                        checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                fullSession.userSet.isNotEmpty()
                            } ?: return@checkAll
                            val user = session.userSet.first()
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val databaseBefore = fullDatabaseDto.encode()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.post(UserRoute.Register()) {
                                setBody(user.toUserServerDto())
                                contentType(ContentType.Application.Json)
                            }
                            response shouldHaveStatus HttpStatusCode.Conflict
                            testTransaction {
                                val session1 = Session.findById(session.id)
                                session1.shouldNotBeNull()
                                session1.lastUser.shouldBeNull()
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
        }
    }
    context("find user") {
        context("with no cookie") {
            should("respond with Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1)
                    ) { fullDatabaseDto, username ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.get(UserRoute.Login(username))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1),
                        Arb.uuid()
                    ) { fullDatabaseDto, username, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.get(UserRoute.Login(username))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("with not a currently existing user from session") {
                should("respond with NotFound" and "last user should be null") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            UserServerDto.arb()
                        ) { fullDatabaseDto, userServerDto ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                fullSession.userSet.isNotEmpty() &&
                                        !fullSession.userSet.map { it.username }
                                            .contains(userServerDto.username)
                            } ?: return@checkAll
                            val databaseBefore = fullDatabaseDto.encode()
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.get(UserRoute.Login(userServerDto.username))
                            response shouldHaveStatus HttpStatusCode.NotFound
                            testTransaction {
                                val session1 = Session.findById(session.id)
                                session1.shouldNotBeNull()
                                session1.lastUser.shouldBeNull()
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with a currently existing user from session") {
                should("respond with OK" and "respond with the corresponding user Id" and "respond with the corresponding email Ids" and "last user should be the found one") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize)
                        ) { fullDatabaseDto ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                fullSession.userSet.isNotEmpty()
                            } ?: return@checkAll
                            val user = session.userSet.first()
                            val databaseBefore = fullDatabaseDto.encode()
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.get(UserRoute.Login(user.username))
                            response shouldHaveStatus HttpStatusCode.OK
                            val responseBody = response.body<UserClientDto>()
                            responseBody.shouldNotBeNull()
                            responseBody.id shouldBe user.id.toString()
                            responseBody.emailIdSet shouldBe user.emailSet.map { it.id.toString() }
                            testTransaction {
                                val session1 = Session.findById(session.id)
                                session1.shouldNotBeNull()
                                session1.lastUser.shouldNotBeNull()
                                session1.lastUser!!.username shouldBe user.username.encode()
                                session1.lastUser!!.session.id.value shouldBe session.id
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
        }
    }
    context("logout user") {
        context("without cookie") {
            should("respond with status Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        UserServerDto.arb()
                    ) { fullDatabaseDto, userServerDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.patch(UserRoute.Logout()) {
                            contentType(ContentType.Application.Json)
                            setBody(userServerDto)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with status Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.uuid(),
                        UserServerDto.arb()
                    ) { fullDatabaseDto, badSessionId, userServerDto ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.patch(UserRoute.Logout()) {
                            contentType(ContentType.Application.Json)
                            setBody(userServerDto)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("without last user") {
                should("respond with NotFound") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            UserServerDto.arb()
                        ) { fullDatabaseDto, userServerDto ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val databaseBefore = fullDatabaseDto.encode()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.patch(UserRoute.Logout()) {
                                setBody(userServerDto)
                                contentType(ContentType.Application.Json)
                            }
                            response shouldHaveStatus HttpStatusCode.NotFound
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with last user") {
                should("respond with OK" and "last user be null" and "last email be null") {
                    testApplication {
                        checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull {
                                it.userSet.isNotEmpty()
                            } ?: return@checkAll
                            val user = session.userSet.first()
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                                exec(session.makeLastUserStatement(user))
                            }
                            val databaseBefore = fullDatabaseDto.encode()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.patch(UserRoute.Logout()) {
                                setBody(user.toUserServerDto())
                                contentType(ContentType.Application.Json)
                            }
                            response shouldHaveStatus HttpStatusCode.OK
                            testTransaction {
                                val session1 = Session.findById(session.id)
                                session1.shouldNotBeNull()
                                session1.lastUser.shouldBeNull()
                                val user1 = User.findById(user.id)
                                user1.shouldNotBeNull()
                                user1.lastEmail.shouldBeNull()
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
        }
    }
    context("import user") {
        context("without cookie") {
            should("respond with status Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        FullUserServerDto.arb(0..maxSize)
                    ) { fullDatabaseDto, fullUserServerDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.post(UserRoute.Import()) {
                            contentType(ContentType.Application.Json)
                            setBody(fullUserServerDto)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with status Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.uuid(),
                        FullUserServerDto.arb(0..maxSize)
                    ) { fullDatabaseDto, badSessionId, fullUserServerDto ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.post(UserRoute.Import()) {
                            contentType(ContentType.Application.Json)
                            setBody(fullUserServerDto)
                        }
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("without currently existing user") {
                should("respond OK" and "have a new user" and "have imported emails and sites to this user" and "respond with a UserIDBDto containing all the names and corresponding ids") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            FullUserServerDto.arb(0..maxSize)
                        ) { fullDatabaseDto, fullUserServerDto ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                !fullSession.userSet.map { it.username }.contains(fullUserServerDto.username)
                            } ?: return@checkAll
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.post(UserRoute.Import()) {
                                contentType(ContentType.Application.Json)
                                setBody(fullUserServerDto)
                            }
                            response shouldHaveStatus HttpStatusCode.OK
                            val responseBody = response.body<UserIDBDto>()
                            responseBody.shouldNotBeNull()
                            testTransaction {
                                val userId =
                                    User.find { Users.username eq fullUserServerDto.username.encode() and (Users.sessionId eq session.id) }
                                        .firstOrNull()?.id?.value
                                userId.shouldNotBeNull()
                                responseBody.toFullUserDto()
                                    .encode() shouldBe FullUserDto.recoverFromDatabase(userId)
                            }
                        }
                    }
                }
            }
            context("with currently existing user") {
                should("respond with Conflict" and "not import the user") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            Arb.set(FullEmailServerDto.arb(0..maxSize), 0..maxSize)
                        ) { fullDatabaseDto, emailSet ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSessionDto ->
                                fullSessionDto.userSet.isNotEmpty()
                            } ?: return@checkAll
                            val user = session.userSet.first()
                            val fullUserServerDto = FullUserServerDto(user.username, emailSet.toMutableSet())
                            val databaseBefore = fullDatabaseDto.encode()
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.post(UserRoute.Import()) {
                                contentType(ContentType.Application.Json)
                                setBody(fullUserServerDto)
                            }
                            response shouldHaveStatus HttpStatusCode.Conflict
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
        }
    }
    context("export session") {
        context("without cookie") {
            should("respond with status Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1)
                    ) { fullDatabaseDto, username ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.get(UserRoute.Export(username))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with status Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.uuid(),
                        Arb.string(minSize = 1)
                    ) { fullDatabaseDto, badSessionId, username ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.get(UserRoute.Export(username))
                        response shouldHaveStatus HttpStatusCode.Unauthorized
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            context("without existing username") {
                should("respond with NotFound") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            Arb.string(minSize = 1)
                        ) { fullDatabaseDto, username ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSessionDto ->
                                fullSessionDto.userSet.isNotEmpty() &&
                                        !fullSessionDto.userSet.map { it.username }.contains(username)
                            } ?: return@checkAll
                            val databaseBefore = fullDatabaseDto.encode()
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.get(UserRoute.Export(username))
                            response shouldHaveStatus HttpStatusCode.NotFound
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with existing username") {
                should("respond with OK" and "with a FullUserClientDto from stored data") {
                    testApplication {
                        checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                            val session = fullDatabaseDto.sessionSet.firstOrNull { fullSessionDto ->
                                fullSessionDto.userSet.isNotEmpty()
                            } ?: return@checkAll
                            val user = session.userSet.first()
                            val databaseBefore = fullDatabaseDto.encode()
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.get(UserRoute.Export(user.username))
                            response shouldHaveStatus HttpStatusCode.OK
                            val responseBody = response.body<FullUserClientDto>()
                            responseBody.shouldNotBeNull()
                            responseBody shouldBe user.toFullUserClientDto()
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
        }
    }
})
