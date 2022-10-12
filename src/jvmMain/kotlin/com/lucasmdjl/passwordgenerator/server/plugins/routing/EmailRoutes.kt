package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.controller.EmailController
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("EmailRoutes")

fun Route.emailRoutes() {

    val emailController by inject<EmailController>()

    post<EmailRoute.New> { emailRoute ->
        logger.debug { call.request.path() }
        emailController.post(call, emailRoute)
    }
    get<EmailRoute.Find> { emailRoute ->
        logger.debug { call.request.path() }
        emailController.get(call, emailRoute)
    }
    delete<EmailRoute.Delete> { emailRoute ->
        logger.debug { call.request.path() }
        emailController.delete(call, emailRoute)
    }
}
