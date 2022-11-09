package com.mypasswordgen.server.controller.impl

import com.mypasswordgen.common.routes.MainRoute
import com.mypasswordgen.server.controller.MainController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class MainControllerImpl : MainController {

    override suspend fun get(call: ApplicationCall, mainRoute: MainRoute) {
        call.respondText(
            this::class.java.classLoader.getResource("html/index.html")!!.readText(),
            ContentType.Text.Html
        )
    }

}
