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

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.idb.SessionIDBDto
import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.kotest.dto.*
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Users
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class SessionTest : FunSpec({
    val testTransaction = initDatabase()

    context("update session") {
        context("without cookie") {
            should("create a new empty session" and "assign the corresponding cookie" and "respond status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val sessionNumber = testTransaction {
                            Sessions.selectAll().count()
                        }
                        val response = client.put(SessionRoute.Update())
                        response shouldHaveStatus HttpStatusCode.OK
                        val sessionId = response.getSessionIdFromCookie()
                        sessionId.shouldNotBeNull()
                        testTransaction {
                            Sessions.selectAll().count() shouldBe sessionNumber + 1
                            val session = Session.findById(sessionId)
                            session.shouldNotBeNull()
                            session.lastUser.shouldBeNull()
                            User.find { Users.sessionId eq sessionId }.shouldBeEmpty()
                            fullDatabaseDto.recoverPartialDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("create a new empty session" and "assign the corresponding cookie" and "respond status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize), Arb.uuid()) { fullDatabaseDto, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val sessionNumber = testTransaction {
                            Sessions.selectAll().count()
                        }
                        val response = client.put(SessionRoute.Update())
                        response shouldHaveStatus HttpStatusCode.OK
                        val sessionId = response.getSessionIdFromCookie()
                        sessionId.shouldNotBeNull()
                        sessionId shouldNotBe badSessionId
                        testTransaction {
                            Sessions.selectAll().count() shouldBe sessionNumber + 1
                            val session = Session.findById(sessionId)
                            session.shouldNotBeNull()
                            session.lastUser.shouldBeNull()
                            User.find { Users.sessionId eq sessionId }.shouldBeEmpty()
                            fullDatabaseDto.recoverPartialDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            should("create a new session" and "move all users to new session" and "delete old session" and "assign the corresponding cookie" and "respond status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val session = fullDatabaseDto.sessionSet.first()
                        val client = createAndConfigureClientWithCookie(session.id)
                        var sessionNumber = 0L
                        var oldUsers = emptyList<UUID>()
                        testTransaction {
                            sessionNumber = Sessions.selectAll().count()
                            oldUsers = User.find { Users.sessionId eq session.id }.map { it.id.value }
                        }
                        val response = client.put(SessionRoute.Update())
                        val sessionId = response.getSessionIdFromCookie()
                        response shouldHaveStatus HttpStatusCode.OK
                        sessionId.shouldNotBeNull()
                        sessionId shouldNotBe session.id
                        testTransaction {
                            Sessions.selectAll().count() shouldBe sessionNumber
                            Session.findById(session.id).shouldBeNull()
                            val newSession = Session.findById(sessionId)
                            newSession.shouldNotBeNull()
                            newSession.lastUser.shouldBeNull()
                            User.forIds(oldUsers).forEach { user ->
                                user.session.id.value shouldBe sessionId
                            }
                        }
                    }
                }
            }
        }
    }
    context("import session") {
        context("without cookie") {
            should("respond with status Unauthorized") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.post(SessionRoute.Import()) {
                            contentType(ContentType.Application.Json)
                            setBody(FullSessionServerDto())
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
                    checkAll(FullDatabaseDto.arb(0..maxSize), Arb.uuid()) { fullDatabaseDto, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.post(SessionRoute.Import()) {
                            contentType(ContentType.Application.Json)
                            setBody(FullSessionServerDto())
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
            context("with existing session") {
                should("respond OK" and "have a new sessionId" and "have imported all users, emails and sites to this session" and "respond with a SessionIDBDto containing all the names and corresponding ids") {
                    testApplication {
                        checkAll(
                            FullDatabaseDto.arb(1..maxSize, 0..maxSize),
                            FullSessionServerDto.arb(0..maxSize)
                        ) { fullDatabaseDto, fullSessionServerDto ->
                            testTransaction {
                                exec(fullDatabaseDto.insertStatement())
                            }
                            val session = fullDatabaseDto.sessionSet.first()
                            val client = createAndConfigureClientWithCookie(session.id)
                            val response = client.post(SessionRoute.Import()) {
                                contentType(ContentType.Application.Json)
                                setBody(fullSessionServerDto)
                            }
                            response shouldHaveStatus HttpStatusCode.OK
                            val sessionId = response.getSessionIdFromCookie()
                            val responseBody = response.body<SessionIDBDto>()
                            responseBody.shouldNotBeNull()
                            sessionId.shouldNotBeNull()
                            responseBody.users.size shouldBe fullSessionServerDto.users.size
                            testTransaction {
                                Session.findById(session.id).shouldBeNull()
                                Session.findById(sessionId).shouldNotBeNull()
                                val fullSessionDatabase = FullSessionDto.recoverFromDatabase(sessionId)
                                fullSessionDatabase.shouldNotBeNull()
                                fullSessionDatabase.toFullSessionServerDto() shouldBe fullSessionServerDto.encode()
                                responseBody.toFullSessionDto(sessionId)
                                    .encode() shouldBe fullSessionDatabase
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
                    checkAll(FullDatabaseDto.arb(0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.get(SessionRoute.Export())
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
                    checkAll(FullDatabaseDto.arb(0..maxSize), Arb.uuid()) { fullDatabaseDto, badSessionId ->
                        fullDatabaseDto.sessionSet.find { it.id != badSessionId }
                            ?: return@checkAll // The session id does exist :(
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.get(SessionRoute.Export())
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
            should("respond with OK" and "with a FullSessionClientDto from stored data") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val session = fullDatabaseDto.sessionSet.first()
                        val client = createAndConfigureClientWithCookie(session.id)
                        val response = client.get(SessionRoute.Export())
                        response shouldHaveStatus HttpStatusCode.OK
                        val responseBody = response.body<FullSessionClientDto>()
                        responseBody.shouldNotBeNull()
                        responseBody shouldBe session.toFullSessionClientDto()
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
    }
})
