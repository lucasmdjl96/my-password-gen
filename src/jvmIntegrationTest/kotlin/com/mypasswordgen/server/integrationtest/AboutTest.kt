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

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.ContributeRoute
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
