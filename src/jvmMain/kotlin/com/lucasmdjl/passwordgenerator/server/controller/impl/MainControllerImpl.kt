package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.routes.MainRoute
import com.lucasmdjl.passwordgenerator.server.controller.MainController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class MainControllerImpl : MainController {

    override suspend fun get(call: ApplicationCall, mainRoute: MainRoute) {
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }

}
