/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.plugins.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("Routes")

fun Application.installRoutes() {

    routing {
        intercept(ApplicationCallPipeline.Monitoring) {
            logger.debug { call.request.path() }
        }
        installHeaders()
        mainRoute()
        sessionRoutes()
        cookieRoutes()
        aboutRoute()
        authenticate("session-auth") {
            authenticatedSessionRoutes()
            userRoutes()
            emailRoutes()
            siteRoutes()
        }
        static("/static") {
            resources("static")
        }
    }
}

inline fun <reified T : Any> Route.get(crossinline controllerFun: suspend (ApplicationCall, T) -> Unit): Route =
    get<T> { t ->
        controllerFun(call, t)
    }

inline fun <reified T : Any> Route.post(crossinline controllerFun: suspend (ApplicationCall, T) -> Unit): Route =
    post<T> { t ->
        controllerFun(call, t)
    }

inline fun <reified T : Any> Route.put(crossinline controllerFun: suspend (ApplicationCall, T) -> Unit): Route =
    put<T> { t ->
        controllerFun(call, t)
    }

inline fun <reified T : Any> Route.delete(crossinline controllerFun: suspend (ApplicationCall, T) -> Unit): Route =
    delete<T> { t ->
        controllerFun(call, t)
    }

inline fun <reified T : Any> Route.patch(crossinline controllerFun: suspend (ApplicationCall, T) -> Unit): Route =
    patch<T> { t ->
        controllerFun(call, t)
    }
