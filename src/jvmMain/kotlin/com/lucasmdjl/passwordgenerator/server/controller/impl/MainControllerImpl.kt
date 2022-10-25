package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.routes.MainRoute
import com.lucasmdjl.passwordgenerator.server.controller.MainController
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*

class MainControllerImpl : MainController {

    override suspend fun get(call: ApplicationCall, mainRoute: MainRoute) {
        call.respond(call.resolveResource("index.html", "html")!!)
    }

}
