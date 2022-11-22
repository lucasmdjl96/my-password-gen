/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.plugins

import com.mypasswordgen.server.test.TestParent
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking

abstract class AuthenticationTestParent : TestParent() {

    fun ApplicationTestBuilder.createAndConfigureClientWithCookie(): HttpClient =
        createClient {
            install(DefaultRequest) {
                host = "localhost"
                port = 443
                url { protocol = URLProtocol.HTTPS }
            }
            install(HttpCookies) {
                runBlocking {
                    storage.addCookie(
                        "https://localhost:443", Cookie(
                            "session",
                            "sessionId%3D%2523s4bc1bd68%2D243a%2D41ec%2Dbdd8%2D29233e04705d",
                            CookieEncoding.RAW
                        )
                    )
                }
            }
        }

    fun ApplicationTestBuilder.createAndConfigureClientWithoutCookie(): HttpClient =
        createClient {
            install(DefaultRequest) {
                host = "localhost"
                port = 443
                url { protocol = URLProtocol.HTTPS }
            }
            install(HttpCookies)
        }
}
