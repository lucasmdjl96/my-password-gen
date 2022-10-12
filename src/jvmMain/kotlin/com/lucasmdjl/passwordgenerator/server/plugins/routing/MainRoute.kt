package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.MainRoute
import com.lucasmdjl.passwordgenerator.server.controller.MainController
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("MainRoute")

fun Route.mainRoute() {

    val mainController by inject<MainController>()

    get<MainRoute> { mainRoute ->
        logger.debug { call.request.path() }
        mainController.get(call, mainRoute)
    }
}
