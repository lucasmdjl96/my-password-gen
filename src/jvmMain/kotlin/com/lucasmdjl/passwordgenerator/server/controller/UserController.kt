package com.lucasmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import io.ktor.server.application.*

interface UserController {

    suspend fun post(call: ApplicationCall, userRoute: UserRoute.Login)

    suspend fun post(call: ApplicationCall, userRoute: UserRoute.Register)

    suspend fun patch(call: ApplicationCall, userRoute: UserRoute.Logout)

}
