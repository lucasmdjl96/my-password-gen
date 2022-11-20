package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.UserRoute
import io.ktor.server.application.*

interface UserController {

    suspend fun post(call: ApplicationCall, userRoute: UserRoute.Login)

    suspend fun post(call: ApplicationCall, userRoute: UserRoute.Register)

    suspend fun patch(call: ApplicationCall, userRoute: UserRoute.Logout)

    suspend fun get(call: ApplicationCall, userRoute: UserRoute.Export)
    suspend fun post(call: ApplicationCall, userRoute: UserRoute.Import)

}
