package com.lucasmdjl.passwordgenerator.server.integrationtest

import com.lucasmdjl.passwordgenerator.common.routes.AboutRoute
import com.lucasmdjl.passwordgenerator.common.routes.ContributeRoute
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AboutTest : TestParent() {

    @Nested
    inner class About {

        @Test
        fun `initial test`() = testApplication {
            val client = createAndConfigureClientWithoutCookie()
            val response = client.get(AboutRoute())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
        }

    }

    @Nested
    inner class Contribute {

        @Test
        fun `initial test`() = testApplication {
            val client = createAndConfigureClientWithoutCookie()
            val response = client.get(ContributeRoute())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Plain, response.contentType()?.withoutParameters())
        }

    }

}
