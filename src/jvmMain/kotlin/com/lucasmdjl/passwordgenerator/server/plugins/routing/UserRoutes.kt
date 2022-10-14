package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.controller.UserController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {

    val userController by inject<UserController>()

    post<UserRoute.Login>(userController::post)
    post<UserRoute.Register>(userController::post)
    patch<UserRoute.Logout>(userController::patch)

}
