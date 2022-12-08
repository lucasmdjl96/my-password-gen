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

import com.mypasswordgen.common.routes.MainRoute
import com.mypasswordgen.server.kotest.dto.FullDatabaseDto
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.server.testing.*

class MainTest : FunSpec({
    val testTransaction = initDatabase()

    context("get main page") {
        context("without cookie") {
            should("respond with Html" and "status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(null)
                        val response = client.get(MainRoute())
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
            should("respond with Html" and "status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(0..maxSize), Arb.uuid()) { fullDatabaseDto, badSessionId ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(badSessionId)
                        val response = client.get(MainRoute())
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
            should("respond with Html" and "status OK") {
                testApplication {
                    checkAll(FullDatabaseDto.arb(1..maxSize, 0..maxSize)) { fullDatabaseDto ->
                        testTransaction {
                            exec(fullDatabaseDto.insertStatement())
                        }
                        val session = fullDatabaseDto.sessionSet.first()
                        val databaseBefore = fullDatabaseDto.encode()
                        val client = createAndConfigureClientWithCookie(session.id)
                        val response = client.get(MainRoute())
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
})
