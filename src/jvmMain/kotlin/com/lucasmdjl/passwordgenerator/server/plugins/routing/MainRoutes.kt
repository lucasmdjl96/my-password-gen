package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.MainRoute
import com.lucasmdjl.passwordgenerator.server.controller.MainController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.mainRoute() {

    val mainController by inject<MainController>()

    get<MainRoute>(mainController::get)

}
