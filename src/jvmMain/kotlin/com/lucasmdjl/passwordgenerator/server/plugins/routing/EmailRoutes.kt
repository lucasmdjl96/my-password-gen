package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.controller.EmailController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.emailRoutes() {

    val emailController by inject<EmailController>()

    post<EmailRoute.New>(emailController::post)
    get<EmailRoute.Find>(emailController::get)
    delete<EmailRoute.Delete>(emailController::delete)

}
