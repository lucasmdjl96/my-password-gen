package com.lucasmdjl.passwordgenerator.server.test.plugins.routing

import com.lucasmdjl.passwordgenerator.server.test.TestParent
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking

abstract class RoutesTestParent : TestParent() {

    fun ApplicationTestBuilder.createAndConfigureClient(): HttpClient =
        createClient {
            install(DefaultRequest) {
                host = "localhost"
                port = 443
                url { protocol = URLProtocol.HTTPS }
            }
            install(Resources)
            install(HttpCookies) {
                runBlocking {
                    storage.addCookie("https://localhost:443",Cookie(
                        "session",
                        "sessionId%3D%2523s4bc1bd68%2D243a%2D41ec%2Dbdd8%2D29233e04705d",
                        CookieEncoding.RAW
                    ))
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }


}
