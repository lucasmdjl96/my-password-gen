package com.mypasswordgen.server.plugins.routing

import com.mypasswordgen.common.routes.EmailRoute
import com.mypasswordgen.server.controller.EmailController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.emailRoutes() {

    val emailController by inject<EmailController>()

    post<EmailRoute.New>(emailController::post)
    get<EmailRoute.Find>(emailController::get)
    delete<EmailRoute.Delete>(emailController::delete)

}
