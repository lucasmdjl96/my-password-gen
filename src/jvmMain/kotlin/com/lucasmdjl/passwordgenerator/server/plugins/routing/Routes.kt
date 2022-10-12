package com.lucasmdjl.passwordgenerator.server.plugins.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.*

fun Application.installRoutes() {
    routing {
        mainRoute()
        sessionRoutes()
        cookieRoutes()
        authenticate("session-auth") {
            userRoutes()
            emailRoutes()
            siteRoutes()
        }
        static("/static") {
            resources()
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
