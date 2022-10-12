package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.server.controller.SessionController
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.put
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("SessionRoutes")

fun Route.sessionRoutes() {

    val sessionController by inject<SessionController>()

    put<SessionRoute> { sessionRoute ->
        logger.debug { call.request.path() }
        sessionController.put(call, sessionRoute)
    }
}
