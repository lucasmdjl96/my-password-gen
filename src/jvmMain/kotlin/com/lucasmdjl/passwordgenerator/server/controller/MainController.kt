package com.lucasmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.routes.MainRoute
import io.ktor.server.application.*

interface MainController {

    suspend fun get(call: ApplicationCall, mainRoute: MainRoute)

}
