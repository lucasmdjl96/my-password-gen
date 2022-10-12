package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.controller.SiteController
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("SiteRoutes")

fun Route.siteRoutes() {

    val siteController by inject<SiteController>()

    post<SiteRoute.New> { siteRoute ->
        logger.debug { call.request.path() }
        siteController.post(call, siteRoute)
    }
    get<SiteRoute.Find> { siteRoute ->
        logger.debug { call.request.path() }
        siteController.get(call, siteRoute)
    }
    delete<SiteRoute.Delete> { siteRoute ->
        logger.debug { call.request.path() }
        siteController.delete(call, siteRoute)
    }
}
