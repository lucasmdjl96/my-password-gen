package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.controller.SiteController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.siteRoutes() {

    val siteController by inject<SiteController>()

    post<SiteRoute.New>(siteController::post)
    get<SiteRoute.Find>(siteController::get)
    delete<SiteRoute.Delete>(siteController::delete)

}
