package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.server.controller.SessionController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.sessionRoutes() {

    val sessionController by inject<SessionController>()

    put<SessionRoute>(sessionController::put)

}
