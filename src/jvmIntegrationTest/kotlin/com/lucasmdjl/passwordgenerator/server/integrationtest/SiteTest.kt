package com.lucasmdjl.passwordgenerator.server.integrationtest

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.tables.Sites
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

class SiteTest : TestParent() {

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
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("site002"))
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
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto("site002"))
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
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
                val initSiteName2 = "SiteXYZ"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto(initSiteName2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertEmpty(Site.find { Sites.siteName eq initSiteName2 })
                }
            }

            @Test
            fun `no last email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
                val initSiteName2 = "Site002"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto(initSiteName2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertEmpty(Site.find { Sites.siteName eq initSiteName2 })
                }
            }

            @Test
            fun `non-existing site`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
                val initSiteName2 = "Site002"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq initEmailId }.count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto(initSiteName2))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<SiteClientDto>()
                assertNotNull(responseBody)
                assertEquals(initSiteName2, responseBody.siteName)
                testTransaction {
                    assertNotEmpty(Site.find { Sites.siteName eq initSiteName2 and (Sites.emailId eq initEmailId) })
                    assertEquals(sitesBefore + 1, Site.find { Sites.emailId eq initEmailId }.count())
                }
            }

            @Test
            fun `site from other email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailId2 = UUID.fromString("75fd4200-6d1e-45fe-8a82-5628e438bc7d")
                val initEmailAddress = "Email001"
                val initEmailAddress2 = "Email002"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId2', '$initEmailAddress2', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId2');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq initEmailId }.count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto(initSiteName))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<SiteClientDto>()
                assertNotNull(responseBody)
                assertEquals(initSiteName, responseBody.siteName)
                testTransaction {
                    assertNotEmpty(Site.find { Sites.siteName eq initSiteName and (Sites.emailId eq initEmailId) })
                    assertEquals(sitesBefore + 1, Site.find { Sites.emailId eq initEmailId }.count())
                }
            }

            @Test
            fun `site from last email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq initEmailId }.count()
                }
                val response = client.post(SiteRoute.New()) {
                    setBody(SiteServerDto(initSiteName))
                    contentType(ContentType.Application.Json)
                }
                assertEquals(HttpStatusCode.Conflict, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Site.find { Sites.emailId eq initEmailId }.count())
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
                val response = client.get(SiteRoute.Find("site002"))
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
                val response = client.get(SiteRoute.Find("site002"))
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
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(SiteRoute.Find(initSiteName))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
            }

            @Test
            fun `no last email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(SiteRoute.Find(initSiteName))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
            }

            @Test
            fun `non-existing site`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
                val initSiteName2 = "Site002"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(SiteRoute.Find(initSiteName2))
                assertEquals(HttpStatusCode.NotFound, response.status)
            }

            @Test
            fun `site from other user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailId2 = UUID.fromString("75fd4200-6d1e-45fe-8a82-5628e438bc7d")
                val initEmailAddress = "Email001"
                val initEmailAddress2 = "Email002"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId2', '$initEmailAddress2', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId2');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(SiteRoute.Find(initSiteName))
                assertEquals(HttpStatusCode.NotFound, response.status)
            }

            @Test
            fun `site from last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val response = client.get(SiteRoute.Find(initSiteName))
                assertEquals(HttpStatusCode.OK, response.status)
                val responseBody = response.body<SiteClientDto>()
                assertNotNull(responseBody)
                assertEquals(initSiteName, responseBody.siteName)
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
                val response = client.delete(SiteRoute.Delete("site002"))
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
                val response = client.delete(SiteRoute.Delete("site002"))
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
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
                testTransaction {
                    exec(
                        """
                        INSERT INTO SESSIONS (ID)
                            VALUES ('$initSessionId');
                        INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                            VALUES ('$initUserId', '$initUsernameEncoded', '$initSessionId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.delete(SiteRoute.Delete(initSiteName))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertNotEmpty(Site.find { Sites.siteName eq initSiteName })
                }
            }

            @Test
            fun `no last email`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Sites.selectAll().count()
                }
                val response = client.delete(SiteRoute.Delete(initSiteName))
                assertEquals(HttpStatusCode.PreconditionFailed, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Sites.selectAll().count())
                    assertNotEmpty(Site.find { Sites.siteName eq initSiteName })
                }
            }

            @Test
            fun `non-existing site`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
                val initSiteName2 = "Site002"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq initEmailId }.count()
                }
                val response = client.delete(SiteRoute.Delete(initSiteName2))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Site.find { Sites.emailId eq initEmailId }.count())
                }
            }

            @Test
            fun `site from other user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailId2 = UUID.fromString("75fd4200-6d1e-45fe-8a82-5628e438bc7d")
                val initEmailAddress = "Email001"
                val initEmailAddress2 = "Email002"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                            VALUES ('$initEmailId2', '$initEmailAddress2', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId2');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq initEmailId }.count()
                }
                val response = client.delete(SiteRoute.Delete(initSiteName))
                assertEquals(HttpStatusCode.NotFound, response.status)
                testTransaction {
                    assertEquals(sitesBefore, Site.find { Sites.emailId eq initEmailId }.count())
                    assertNotNull(Site.findById(initSiteId))
                }
            }

            @Test
            fun `site from last user`() = testApplication {
                val initSessionId = UUID.fromString("e306f416-0e6a-46b7-bec3-bb6c01ae9b1d")
                val initUserId = UUID.fromString("56c7e9f2-fc75-4f1d-8c75-911a867a8811")
                val initUsername = "User123"
                val initUsernameEncoded = initUsername.encode()
                val initEmailId = UUID.fromString("092ff7ae-88b5-4dd9-8ad9-c273d6ad2647")
                val initEmailAddress = "Email001"
                val initSiteId = UUID.fromString("44c3c74c-b0bb-402d-83cf-4ca448e98e71")
                val initSiteName = "Site001"
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
                            VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                        UPDATE USERS
                            SET LAST_EMAIL_ID = '$initEmailId'
                            WHERE ID = '$initUserId';
                        INSERT INTO SITES (ID,SITE_NAME, EMAIL_ID)
                            VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    """.trimIndent()
                    )
                }
                val client = createAndConfigureClientWithCookie(initSessionId)
                val sitesBefore = testTransaction {
                    Site.find { Sites.emailId eq initEmailId }.count()
                }
                val response = client.delete(SiteRoute.Delete(initSiteName))
                assertEquals(HttpStatusCode.OK, response.status)
                testTransaction {
                    assertEquals(sitesBefore - 1, Site.find { Sites.emailId eq initEmailId }.count())
                    assertNull(Site.findById(initSiteId))
                }
            }

        }

    }

}
