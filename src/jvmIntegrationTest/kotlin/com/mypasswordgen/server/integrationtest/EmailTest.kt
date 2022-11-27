/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.integrationtest

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.routes.EmailRoute
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.tables.Emails
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EmailTest : TestParent() {

    @Nested
    inner class Create {

        @Nested
        inner class Challenged {

            @Test
            fun `no cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithoutCookie()
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto("email002"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("a775dc6b-9ceb-4968-867b-eae34d30903a")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId2)
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto("email002"))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                val initEmailAddress2 = "EmailXXX"
                val initEmailAddress2Encoded = initEmailAddress2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Emails.selectAll().count()
                }
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto(initEmailAddress2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(emailsBefore, Emails.selectAll().count())
                    assertEmpty(Email.find { Emails.emailAddress eq initEmailAddress2Encoded })
                }
            }

            @Test
            fun `non-existing email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                val initEmailAddress2 = "EmailXXX"
                val initEmailAddress2Encoded = initEmailAddress2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq initUserId }.count()
                }
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto(initEmailAddress2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<EmailClientDto>()
                assertEquals(setOf(), responseBody.siteIdSet)
                testTransaction {
                    assertNotEmpty(Email.find { Emails.userId eq initUserId and (Emails.emailAddress eq initEmailAddress2Encoded) })
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNotNull(user.lastEmail)
                    assertEquals(initEmailAddress2Encoded, user.lastEmail!!.emailAddress)
                    assertEquals(initUserId, user.lastEmail!!.user.id.value)
                    assertEquals(emailsBefore + 1, Email.find { Emails.userId eq initUserId }.count())
                }
            }

            @Test
            fun `email from other user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUserId2 = UUID.fromString("f60b0339-b1fc-4b07-90af-b38c7a91fd73")
                val initUsername = "User123"
                val initUsername2 = "UserABC"
                val initUsernameEncoded = initUsername.encode()
                val initUsername2Encoded = initUsername2.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId2');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq initUserId }.count()
                }
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto(initEmailAddress))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<EmailClientDto>()
                assertEquals(setOf(), responseBody.siteIdSet)
                testTransaction {
                    assertNotEmpty(Email.find { Emails.userId eq initUserId and (Emails.emailAddress eq initEmailAddressEncoded) })
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNotNull(user.lastEmail)
                    assertEquals(initEmailAddressEncoded, user.lastEmail!!.emailAddress)
                    assertEquals(initUserId, user.lastEmail!!.user.id.value)
                    assertEquals(emailsBefore + 1, Email.find { Emails.userId eq initUserId }.count())
                }
            }

            @Test
            fun `email from last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq initUserId }.count()
                }
                val response = client.post(EmailRoute.New()) {
                    setBody(EmailServerDto(initEmailAddress))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Conflict, response.status)
                testTransaction {
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore, Email.find { Emails.userId eq initUserId }.count())
                }
            }
        }

    }

    @Nested
    inner class Find {

        @Nested
        inner class Challenged {

            @Test
            fun `no cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithoutCookie()
                val response = client.get(EmailRoute.Find("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("a775dc6b-9ceb-4968-867b-eae34d30903a")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId2)
                val response = client.get(EmailRoute.Find("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(EmailRoute.Find(initEmailAddress))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
            }

            @Test
            fun `non-existing email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                val initEmailAddress2 = "Email002"
                val initEmailAddress2Encoded = initEmailAddress2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(EmailRoute.Find(initEmailAddress2))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                }
            }

            @Test
            fun `email from other user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUserId2 = UUID.fromString("f60b0339-b1fc-4b07-90af-b38c7a91fd73")
                val initUsername = "User123"
                val initUsername2 = "UserABC"
                val initUsernameEncoded = initUsername.encode()
                val initUsername2Encoded = initUsername2.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId2');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(EmailRoute.Find(initEmailAddress))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                }
            }

            @Test
            fun `email from last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteId2 = UUID.fromString("dc4c172a-6d2f-4242-9bac-f9e594fb4a21")
                val initSiteName = "Site001"
                val initSiteNameEncoded = initSiteName.encode()
                val initSiteName2 = "Site002"
                val initSiteName2Encoded = initSiteName2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteNameEncoded', '$initEmailId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId2', '$initSiteName2Encoded', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(EmailRoute.Find(initEmailAddress))
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<EmailClientDto>()
                assertEquals(setOf(initSiteId.toString(), initSiteId2.toString()), responseBody.siteIdSet)
                testTransaction {
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNotNull(user.lastEmail)
                    assertEquals(initEmailAddressEncoded, user.lastEmail!!.emailAddress)
                    assertEquals(initUserId, user.lastEmail!!.user.id.value)
                }
            }
        }

    }

    @Nested
    inner class Delete {

        @Nested
        inner class Challenged {

            @Test
            fun `no cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithoutCookie()
                val response = client.delete(EmailRoute.Delete("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            @Test
            fun `bad cookie`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initSessionId2 = UUID.fromString("a775dc6b-9ceb-4968-867b-eae34d30903a")
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId2)
                val response = client.delete(EmailRoute.Delete("email002"))
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

        }

        @Nested
        inner class Validated {

            @Test
            fun `no last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Emails.selectAll().count()
                }
                val response = client.delete(EmailRoute.Delete(initEmailAddress))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(emailsBefore, Emails.selectAll().count())
                    assertNotEmpty(Email.find { Emails.emailAddress eq initEmailAddressEncoded })
                }
            }

            @Test
            fun `non-existing email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                val initEmailAddress2 = "Email002"
                val initEmailAddress2Encoded = initEmailAddress2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq initUserId }.count()
                }
                val response = client.delete(EmailRoute.Delete(initEmailAddress2))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore, Email.find { Emails.userId eq initUserId }.count())
                }
            }

            @Test
            fun `email from other user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUserId2 = UUID.fromString("f60b0339-b1fc-4b07-90af-b38c7a91fd73")
                val initUsername = "User123"
                val initUsername2 = "UserABC"
                val initUsernameEncoded = initUsername.encode()
                val initUsername2Encoded = initUsername2.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId2', '$initUsername2Encoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId2');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq initUserId }.count()
                }
                val response = client.delete(EmailRoute.Delete(initEmailAddress))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore, Email.find { Emails.userId eq initUserId }.count())
                    assertNotNull(Email.findById(initEmailId))
                }
            }

            @Test
            fun `email from last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initEmailAddressEncoded = initEmailAddress.encode()
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteId2 = UUID.fromString("dc4c172a-6d2f-4242-9bac-f9e594fb4a21")
                val initSiteName = "Site001"
                val initSiteNameEncoded = initSiteName.encode()
                val initSiteName2 = "Site002"
                val initSiteName2Encoded = initSiteName2.encode()
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        UPDATE SESSIONS
                            SET LAST_USER_ID = '$initUserId'
                            WHERE ID = '$initSessionId';
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddressEncoded', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteNameEncoded', '$initEmailId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId2', '$initSiteName2Encoded', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val emailsBefore = testTransaction {
                    Email.find { Emails.userId eq initUserId }.count()
                }
                val response = client.delete(EmailRoute.Delete(initEmailAddress))
                assertEquals(HttpStatusCode.OK, response.status)
                testTransaction {
                    val user = User.findById(initUserId)
                    assertNotNull(user)
                    assertNull(user.lastEmail)
                    assertEquals(emailsBefore - 1, Email.find { Emails.userId eq initUserId }.count())
                    assertNull(Email.findById(initEmailId))
                }
            }
        }

    }

}
