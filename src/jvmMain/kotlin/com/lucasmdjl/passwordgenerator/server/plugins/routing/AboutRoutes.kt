package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.AboutRoute
import com.lucasmdjl.passwordgenerator.common.routes.ContributeRoute
import com.lucasmdjl.passwordgenerator.server.controller.AboutController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.aboutRoute() {

    val aboutController by inject<AboutController>()

    get<AboutRoute>(aboutController::get)
    get<ContributeRoute>(aboutController::get)

}
