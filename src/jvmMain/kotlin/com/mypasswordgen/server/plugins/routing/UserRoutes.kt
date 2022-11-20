package com.mypasswordgen.server.plugins.routing

import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.server.controller.UserController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {

    val userController by inject<UserController>()

    post<UserRoute.Login>(userController::post)
    post<UserRoute.Register>(userController::post)
    patch<UserRoute.Logout>(userController::patch)
    get<UserRoute.Export>(userController::get)
    post<UserRoute.Import>(userController::post)

}
