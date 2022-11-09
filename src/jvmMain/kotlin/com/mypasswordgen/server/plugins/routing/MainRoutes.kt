package com.mypasswordgen.server.plugins.routing

import com.mypasswordgen.common.routes.MainRoute
import com.mypasswordgen.server.controller.MainController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.mainRoute() {

    val mainController by inject<MainController>()

    get<MainRoute>(mainController::get)

}
