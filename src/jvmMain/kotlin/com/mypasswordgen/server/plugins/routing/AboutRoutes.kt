package com.mypasswordgen.server.plugins.routing

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.ContributeRoute
import com.mypasswordgen.server.controller.AboutController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.aboutRoute() {

    val aboutController by inject<AboutController>()

    get<AboutRoute>(aboutController::get)
    get<ContributeRoute>(aboutController::get)

}
