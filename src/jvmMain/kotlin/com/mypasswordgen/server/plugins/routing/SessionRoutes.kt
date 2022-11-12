package com.mypasswordgen.server.plugins.routing

import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.controller.SessionController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.sessionRoutes() {

    val sessionController by inject<SessionController>()

    put<SessionRoute>(sessionController::put)

}