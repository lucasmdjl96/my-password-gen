package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.MainRoute
import io.ktor.server.application.*

interface MainController {

    suspend fun get(call: ApplicationCall, mainRoute: MainRoute)

}
