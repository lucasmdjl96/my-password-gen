package com.mypasswordgen.server.integrationtest

import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.tables.Users
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CookieTest : TestParent() {

    @Nested
    inner class Policy {
        @Test
        fun `get cookie policy`() = testApplication {
            val client = createAndConfigureClientWithoutCookie()
            val response = client.get(CookieRoute.Policy())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
        }
    }

    @Nested
    inner class OptOut {

        @Test
        fun `cookie opt out without cookies`() = testApplication {
            val client = createAndConfigureClientWithoutCookie()
            val response = client.get(CookieRoute.OptOut())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            assertEmpty(client.cookies("/"))
        }

        @Test
        fun `cookie opt out with cookie`() = testApplication {
            val initSessionId = UUID.fromString("e93a78e5-6154-4411-97d6-0c0510b7442c")
            val initUserId = UUID.fromString("f8d7d3f3-53c3-4e1b-aa7f-ed1ef636f732")
            val initUserId2 = UUID.fromString("b2155d63-4b2c-4096-aee9-4ba7905186c0")
            val initUsername = "User123"
            val initUsername2 = "User234"
            val initEmailId = UUID.fromString("1a6dd8e3-c5c9-4782-ba94-7451714472c3")
            val initEmailId2 = UUID.fromString("16cfd1d8-1e05-411d-8f0a-c1c04a48650d")
            val initEmailAddress = "Email001"
            val initEmailAddress2 = "Email002"
            val initSiteId = UUID.fromString("bb5de836-878c-4d2f-a5b5-2bfa407bd3ce")
            val initSiteId2 = UUID.fromString("333e53b0-b5d1-4247-94e2-893354338960")
            val initSiteName = "SiteXXX"
            val initSiteName2 = "SiteABC"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId', '$initUsername', '$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId2', '$initUsername2', '$initSessionId');
                    INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                        VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                    INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                        VALUES ('$initEmailId2', '$initEmailAddress2', '$initUserId2');
                    INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                        VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                        VALUES ('$initSiteId2', '$initSiteName2', '$initEmailId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId)
            val response = client.get(CookieRoute.OptOut())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            testTransaction {
                assertNull(Session.findById(initSessionId))
                assertEmpty(User.find { Users.sessionId eq initSessionId })
                for (id in listOf(initUserId, initUserId2)) assertNull(User.findById(id))
                for (id in listOf(initEmailId, initEmailId2)) assertNull(Email.findById(id))
                for (id in listOf(initSiteId, initSiteId2)) assertNull(Site.findById(id))
            }
            assertEmpty(client.cookies("/"))
        }

        @Test
        fun `cookie opt out with bad cookie`() = testApplication {
            val initSessionId = UUID.fromString("e93a78e5-6154-4411-97d6-0c0510b7442c")
            val initSessionId2 = UUID.fromString("2cab01cf-78f3-4e44-af36-cc534f315c6e")
            val initUserId = UUID.fromString("f8d7d3f3-53c3-4e1b-aa7f-ed1ef636f732")
            val initUserId2 = UUID.fromString("b2155d63-4b2c-4096-aee9-4ba7905186c0")
            val initUsername = "User123"
            val initUsername2 = "User234"
            val initEmailId = UUID.fromString("1a6dd8e3-c5c9-4782-ba94-7451714472c3")
            val initEmailId2 = UUID.fromString("16cfd1d8-1e05-411d-8f0a-c1c04a48650d")
            val initEmailAddress = "Email001"
            val initEmailAddress2 = "Email002"
            val initSiteId = UUID.fromString("bb5de836-878c-4d2f-a5b5-2bfa407bd3ce")
            val initSiteId2 = UUID.fromString("333e53b0-b5d1-4247-94e2-893354338960")
            val initSiteName = "SiteXXX"
            val initSiteName2 = "SiteABC"
            testTransaction {
                exec(
                    """
                    INSERT INTO SESSIONS (ID)
                        VALUES ('$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId', '$initUsername', '$initSessionId');
                    INSERT INTO USERS (ID, USERNAME, SESSION_ID)
                        VALUES ('$initUserId2', '$initUsername2', '$initSessionId');
                    INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                        VALUES ('$initEmailId', '$initEmailAddress', '$initUserId');
                    INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
                        VALUES ('$initEmailId2', '$initEmailAddress2', '$initUserId2');
                    INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                        VALUES ('$initSiteId', '$initSiteName', '$initEmailId');
                    INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
                        VALUES ('$initSiteId2', '$initSiteName2', '$initEmailId');
                """.trimIndent()
                )
            }
            val client = createAndConfigureClientWithCookie(initSessionId2)
            val response = client.get(CookieRoute.OptOut())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            testTransaction {
                assertNull(Session.findById(initSessionId2))
                assertEmpty(User.find { Users.sessionId eq initSessionId2 })
            }
            assertEmpty(client.cookies("/"))
        }

    }

}
