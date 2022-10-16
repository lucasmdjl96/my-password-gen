package com.lucasmdjl.passwordgenerator.server.integrationtest

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.tables.Users
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
            val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f0001")
            val client = createAndConfigureClientWithCookie(sessionId)
            val response = client.get(CookieRoute.OptOut())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            testTransaction {
                assertNull(Session.findById(sessionId))
                assertEmpty(User.find { Users.sessionId eq sessionId })
                for (i in 2..4) assertNull(User.findById(i))
                for (i in 2..7) assertNull(Email.findById(i))
                for (i in 2..12) assertNull(Site.findById(i))
            }
            assertEmpty(client.cookies("/"))
        }

        @Test
        fun `cookie opt out with bad cookie`() = testApplication {
            val sessionId = UUID.fromString("757f2ad6-aa06-4403-aea3-d5e6cb9f9999")
            val client = createAndConfigureClientWithCookie(sessionId)
            val response = client.get(CookieRoute.OptOut())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
            testTransaction {
                assertNull(Session.findById(sessionId))
                assertEmpty(User.find { Users.sessionId eq sessionId })
            }
            assertEmpty(client.cookies("/"))
        }

    }

}
