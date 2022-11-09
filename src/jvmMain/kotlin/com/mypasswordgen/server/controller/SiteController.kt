package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.SiteRoute
import io.ktor.server.application.*

interface SiteController {

    suspend fun post(call: ApplicationCall, siteRoute: SiteRoute.New)

    suspend fun get(call: ApplicationCall, siteRoute: SiteRoute.Find)

    suspend fun delete(call: ApplicationCall, siteRoute: SiteRoute.Delete)

}
