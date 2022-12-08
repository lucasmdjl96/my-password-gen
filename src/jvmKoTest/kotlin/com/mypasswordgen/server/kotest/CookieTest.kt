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

import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.server.kotest.dto.FullDatabaseDto
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.server.testing.*

class CookieTest : FunSpec({
    val testTransaction = initDatabase()

    context("get cookie policy") {
        context("without cookie") {
            should("respond Html" and "status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.get(CookieRoute.Policy())
                        response shouldHaveStatus HttpStatusCode.OK
                        response shouldHaveContentType ContentType.Text.Html
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond Html" and "status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize), Arb.uuid()) { fullDatabaseDto, badSessionId ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.get(CookieRoute.Policy())
                        response shouldHaveStatus HttpStatusCode.OK
                        response shouldHaveContentType ContentType.Text.Html
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with good cookie") {
            should("respond Html" and "status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val session = fullDatabaseDto.sessionSet.first()
                        val client = createAndConfigureClientWithCookie(session.id)
                        val response = client.get(CookieRoute.Policy())
                        response shouldHaveStatus HttpStatusCode.OK
                        response shouldHaveContentType ContentType.Text.Html
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
    }
    context("cookie opt-out") {
        context("without cookie") {
            should("respond with Html" and "status OK" and "no cookies") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.get(CookieRoute.OptOut())
                        response shouldHaveStatus HttpStatusCode.OK
                        response shouldHaveContentType ContentType.Text.Html
                        client.cookies("/").shouldBeEmpty()
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                    }
                }
            }
        }
        context("with bad cookie") {
            should("respond with Html" and "status OK" and "clear cookies") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize), Arb.uuid()) { fullDatabaseDto, badSessionId ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.get(CookieRoute.OptOut())
                        response shouldHaveStatus HttpStatusCode.OK
                        response shouldHaveContentType ContentType.Text.Html
                        testTransaction {
                            FullDatabaseDto.recoverFromDatabase() shouldBe databaseBefore
                            cleanUp()
                        }
                        client.cookies("/").shouldBeEmpty()
                    }
                }
            }
        }
        context("with good cookie") {
            should("respond with Html" and "status OK" and "clear cookies" and "remove data from database") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val session = fullDatabaseDto.sessionSet.first()
                        val client = createAndConfigureClientWithCookie(session.id)
                        val response = client.get(CookieRoute.OptOut())
                        response shouldHaveStatus HttpStatusCode.OK
                        response shouldHaveContentType ContentType.Text.Html
                        testTransaction {
                            Session.findById(session.id).shouldBeNull()
                            User.find { Users.sessionId eq session.id }.shouldBeEmpty()
                            for (user in session.userSet) {
                                User.findById(user.id).shouldBeNull()
                                Email.find { Emails.userId eq user.id }.shouldBeEmpty()
                                for (email in user.emailSet) {
                                    Email.findById(email.id).shouldBeNull()
                                    Site.find { Sites.emailId eq email.id }.shouldBeEmpty()
                                    for (site in email.siteSet) {
                                        Site.findById(site.id).shouldBeNull()
                                    }
                                }
                            }
                        }
                        client.cookies("/").shouldBeEmpty()
                    }
                }
            }
        }
    }
})
