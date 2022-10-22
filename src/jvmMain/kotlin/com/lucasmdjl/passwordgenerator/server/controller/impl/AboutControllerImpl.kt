package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.routes.AboutRoute
import com.lucasmdjl.passwordgenerator.server.controller.AboutController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class AboutControllerImpl : AboutController {

    override suspend fun get(call: ApplicationCall, aboutRoute: AboutRoute) {
        call.respondText(
            this::class.java.classLoader.getResource("about.html")!!.readText(),
            ContentType.Text.Html
        )
    }

}
