package com.mypasswordgen.server.integrationtest

import com.mypasswordgen.common.routes.MainRoute
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MainTest : TestParent() {

    @Nested
    inner class Main {

        @Test
        fun `initial test`() = testApplication {
            val client = createAndConfigureClientWithoutCookie()
            val response = client.get(MainRoute())
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html, response.contentType()?.withoutParameters())
        }

    }


}
