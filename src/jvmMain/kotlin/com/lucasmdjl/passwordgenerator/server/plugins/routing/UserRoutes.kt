package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.controller.UserController
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("UserRoutes")

fun Route.userRoutes() {

    val userController by inject<UserController>()

    post<UserRoute.Login> { userRoute ->
        logger.debug { call.request.path() }
        userController.post(call, userRoute)
    }
    post<UserRoute.Register> { userRoute ->
        logger.debug { call.request.path() }
        userController.post(call, userRoute)
    }
}
