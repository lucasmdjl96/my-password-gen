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

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.routes.EmailRoute
import com.mypasswordgen.server.kotest.dto.*
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.tables.Emails
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and

class EmailTest : FunSpec({
    val testTransaction = initDatabase()

    context("create email") {
        context("with no cookie") {
            should("respond with Unauthorized" and "not create any email") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        EmailServerDto.arb()
                    ) { fullDatabaseDto, emailServerDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.post(EmailRoute.New()) {
                            setBody(emailServerDto)
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
            should("respond with Unauthorized" and "not create any email") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        EmailServerDto.arb(),
                        Arb.uuid()
                    ) { fullDatabaseDto, emailServerDto, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.post(EmailRoute.New()) {
                            setBody(emailServerDto)
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
            context("without last user") {
                should("respond with PreconditionFailed" and "not create any email") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            EmailServerDto.arb()
                        ) { fullDatabaseDto, emailServerDto ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val databaseBefore = fullDatabaseDto.encode()
                            val response = client.post(EmailRoute.New()) {
                                setBody(emailServerDto)
                                contentType(ContentType.Application.Json)
                            }
                            response shouldHaveStatus HttpStatusCode.PreconditionFailed
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with last user") {
                context("with not a currently existing email from last user") {
                    should("respond with OK" and "respond with empty list of site ids" and "create the email on last user" and "set the email as last email") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                EmailServerDto.arb()
                            ) { fullDatabaseDto, emailServerDto ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                    fullSession.userSet.isNotEmpty() &&
                                            !fullSession.userSet.flatMap { it.emailSet }
                                                .map { it.emailAddress }
                                                .contains(emailServerDto.emailAddress)
                                } ?: return@checkAll
                                val lastUser = session.userSet.firstOrNull { fullUser ->
                                    fullUser.emailSet.isNotEmpty() &&
                                            !fullUser.emailSet.map { it.emailAddress }
                                                .contains(emailServerDto.emailAddress)
                                } ?: return@checkAll
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val emailsBefore = testTransaction {
                                    Email.find { Emails.userId eq lastUser.id }.count()
                                }
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.post(EmailRoute.New()) {
                                    setBody(emailServerDto)
                                    contentType(ContentType.Application.Json)
                                }
                                response shouldHaveStatus HttpStatusCode.OK
                                val responseBody = response.body<EmailClientDto>()
                                responseBody.shouldNotBeNull()
                                responseBody.siteIdSet.shouldBeEmpty()
                                testTransaction {
                                    Email.find { Emails.userId eq lastUser.id and (Emails.emailAddress eq emailServerDto.emailAddress.encode()) }
                                        .shouldNotBeEmpty()
                                    val user = User.findById(lastUser.id)
                                    user.shouldNotBeNull()
                                    user.lastEmail.shouldNotBeNull()
                                    user.lastEmail!!.emailAddress shouldBe emailServerDto.emailAddress.encode()
                                    user.lastEmail!!.user.id.value shouldBe lastUser.id
                                    Email.find { Emails.userId eq lastUser.id }
                                        .count() shouldBe emailsBefore + 1
                                    for (fullSessionDto in fullDatabaseDto.sessionSet.filterNot { it.id == session.id }) {
                                        FullSessionDto.recoverFromDatabase(fullSessionDto.id) shouldBe fullSessionDto.encode()
                                    }
                                    for (fullUserDto in session.userSet.filterNot { it.id == lastUser.id }) {
                                        FullUserDto.recoverFromDatabase(fullUserDto.id) shouldBe fullUserDto.encode()
                                    }
                                    for (fullEmailDto in lastUser.emailSet.filterNot { it.emailAddress == emailServerDto.emailAddress.encode() }) {
                                        FullEmailDto.recoverFromDatabase(fullEmailDto.id) shouldBe fullEmailDto.encode()
                                    }
                                }
                            }
                        }
                    }
                }
                context("with a currently existing email from last user") {
                    should("respond with Conflict" and "last email should be null" and "not create any email") {
                        testApplication {
                            checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                    fullSession.userSet.flatMap { it.emailSet }.isNotEmpty()
                                } ?: return@checkAll
                                val lastUser = session.userSet.firstOrNull { fullUser ->
                                    fullUser.emailSet.isNotEmpty()
                                } ?: return@checkAll
                                val email = lastUser.emailSet.first()
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val databaseBefore = fullDatabaseDto.encode()
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.post(EmailRoute.New()) {
                                    setBody(email.toEmailServerDto())
                                    contentType(ContentType.Application.Json)
                                }
                                response shouldHaveStatus HttpStatusCode.Conflict
                                testTransaction {
                                    val user = User.findById(lastUser.id)
                                    user.shouldNotBeNull()
                                    user.lastEmail.shouldBeNull()
                                    FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                    cleanUp()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    context("find email") {
        context("with no cookie") {
            should("respond with Unauthorized") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1)
                    ) { fullDatabaseDto, emailAddress ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.get(EmailRoute.Find(emailAddress))
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
                    ) { fullDatabaseDto, emailAddress, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.get(EmailRoute.Find(emailAddress))
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
                should("respond with PreconditionFailed") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            Arb.string(minSize = 1)
                        ) { fullDatabaseDto, emailAddress ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val databaseBefore = fullDatabaseDto.encode()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.get(EmailRoute.Find(emailAddress))
                            response shouldHaveStatus HttpStatusCode.PreconditionFailed
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with last user") {
                context("with not a currently existing email from last user") {
                    should("respond with NotFound" and "last email should be null") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                EmailServerDto.arb()
                            ) { fullDatabaseDto, emailServerDto ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                    fullSession.userSet.isNotEmpty() &&
                                            !fullSession.userSet.flatMap { it.emailSet }
                                                .map { it.emailAddress }
                                                .contains(emailServerDto.emailAddress)
                                } ?: return@checkAll
                                val lastUser = session.userSet.firstOrNull { fullUser ->
                                    fullUser.emailSet.isNotEmpty() &&
                                            !fullUser.emailSet.map { it.emailAddress }
                                                .contains(emailServerDto.emailAddress)
                                } ?: return@checkAll
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val databaseBefore = fullDatabaseDto.encode()
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.get(EmailRoute.Find(emailServerDto.emailAddress))
                                response shouldHaveStatus HttpStatusCode.NotFound
                                testTransaction {
                                    val user = User.findById(lastUser.id)
                                    user.shouldNotBeNull()
                                    user.lastEmail.shouldBeNull()
                                    FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                    cleanUp()
                                }
                            }
                        }
                    }
                }
                context("with a currently existing email from last user") {
                    should("respond with OK" and "respond with the corresponding email Id" and "respond with the corresponding site Ids" and "last email should be the found one") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize)
                            ) { fullDatabaseDto ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                    fullSession.userSet.flatMap { it.emailSet }.isNotEmpty()
                                } ?: return@checkAll
                                val lastUser = session.userSet.firstOrNull { fullUser ->
                                    fullUser.emailSet.isNotEmpty()
                                } ?: return@checkAll
                                val email = lastUser.emailSet.first()
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val databaseBefore = fullDatabaseDto.encode()
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.get(EmailRoute.Find(email.emailAddress))
                                response shouldHaveStatus HttpStatusCode.OK
                                val responseBody = response.body<EmailClientDto>()
                                responseBody.shouldNotBeNull()
                                responseBody.id shouldBe email.id.toString()
                                responseBody.siteIdSet shouldBe email.siteSet.map { it.id.toString() }
                                testTransaction {
                                    val user = User.findById(lastUser.id)
                                    user.shouldNotBeNull()
                                    user.lastEmail.shouldNotBeNull()
                                    user.lastEmail!!.emailAddress shouldBe email.emailAddress.encode()
                                    user.lastEmail!!.user.id.value shouldBe lastUser.id
                                    FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                    cleanUp()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    context("delete email") {
        context("with no cookie") {
            should("respond with Unauthorized" and "not delete any email") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1)
                    ) { fullDatabaseDto, emailAddress ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(null)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.delete(EmailRoute.Delete(emailAddress))
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
            should("respond with Unauthorized" and "not delete any email") {
                testApplication {
                    checkAll(
                        FullDatabaseDto.arb(0..maxSize),
                        Arb.string(minSize = 1),
                        Arb.uuid()
                    ) { fullDatabaseDto, emailAddress, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val databaseBefore = fullDatabaseDto.encode()
                        val response = client.delete(EmailRoute.Delete(emailAddress))
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
                should("respond with PreconditionFailed" and "not delete any email") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            Arb.string(minSize = 1)
                        ) { fullDatabaseDto, emailAddress ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val databaseBefore = fullDatabaseDto.encode()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.delete(EmailRoute.Delete(emailAddress))
                            response shouldHaveStatus HttpStatusCode.PreconditionFailed
                            testTransaction {
                                FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                cleanUp()
                            }
                        }
                    }
                }
            }
            context("with last user") {
                context("with not a currently existing email from last user") {
                    should("respond with NotFound" and "last email should be null" and "not delete any email") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                                EmailServerDto.arb()
                            ) { fullDatabaseDto, emailServerDto ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                    fullSession.userSet.isNotEmpty() &&
                                            !fullSession.userSet.flatMap { it.emailSet }
                                                .map { it.emailAddress }
                                                .contains(emailServerDto.emailAddress)
                                } ?: return@checkAll
                                val lastUser = session.userSet.firstOrNull { fullUser ->
                                    fullUser.emailSet.isNotEmpty() &&
                                            !fullUser.emailSet.map { it.emailAddress }
                                                .contains(emailServerDto.emailAddress)
                                } ?: return@checkAll
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val databaseBefore = fullDatabaseDto.encode()
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.delete(EmailRoute.Delete(emailServerDto.emailAddress))
                                response shouldHaveStatus HttpStatusCode.NotFound
                                testTransaction {
                                    val user = User.findById(lastUser.id)
                                    user.shouldNotBeNull()
                                    user.lastEmail.shouldBeNull()
                                    FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                                    cleanUp()
                                }
                            }
                        }
                    }
                }
                context("with a currently existing email from last user") {
                    should("respond with OK" and "last email should be null" and "the email should be deleted" and "respond with the corresponding email and site ids") {
                        testApplication {
                            checkAll(
                                FullDatabaseDto.arb(1..maxSize, 0..maxSize)
                            ) { fullDatabaseDto ->
                                val session = fullDatabaseDto.sessionSet.firstOrNull { fullSession ->
                                    fullSession.userSet.flatMap { it.emailSet }.isNotEmpty()
                                } ?: return@checkAll
                                val lastUser = session.userSet.firstOrNull { fullUser ->
                                    fullUser.emailSet.isNotEmpty()
                                } ?: return@checkAll
                                val email = lastUser.emailSet.first()
                                testTransaction {
                                    exec(fullDatabaseDto.insertStatement())
                                    exec(session.makeLastUserStatement(lastUser))
                                }
                                val emailsBefore = testTransaction {
                                    Email.find { Emails.userId eq lastUser.id }.count()
                                }
                                val client = createAndConfigureClientWithCookie(session.id)
                                val response = client.delete(EmailRoute.Delete(email.emailAddress))
                                response shouldHaveStatus HttpStatusCode.OK
                                val responseBody = response.body<EmailClientDto>()
                                responseBody.shouldNotBeNull()
                                responseBody.id shouldBe email.id.toString()
                                responseBody.siteIdSet shouldBe email.siteSet.map { it.id.toString() }
                                testTransaction {
                                    val user = User.findById(lastUser.id)
                                    user.shouldNotBeNull()
                                    user.lastEmail.shouldBeNull()
                                    Email.find { Emails.userId eq lastUser.id }
                                        .count() shouldBe emailsBefore - 1
                                    Email.findById(email.id).shouldBeNull()
                                    for (fullSessionDto in fullDatabaseDto.sessionSet.filterNot { it.id == session.id }) {
                                        FullSessionDto.recoverFromDatabase(fullSessionDto.id) shouldBe fullSessionDto.encode()
                                    }
                                    for (fullUserDto in session.userSet.filterNot { it.id == lastUser.id }) {
                                        FullUserDto.recoverFromDatabase(fullUserDto.id) shouldBe fullUserDto.encode()
                                    }
                                    for (fullEmailDto in lastUser.emailSet.filterNot { it.id == email.id }) {
                                        FullEmailDto.recoverFromDatabase(fullEmailDto.id) shouldBe fullEmailDto.encode()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
})
