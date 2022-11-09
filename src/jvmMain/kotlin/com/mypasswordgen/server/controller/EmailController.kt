package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.EmailRoute
import io.ktor.server.application.*

interface EmailController {

    suspend fun post(call: ApplicationCall, emailRoute: EmailRoute.New)

    suspend fun get(call: ApplicationCall, emailRoute: EmailRoute.Find)

    suspend fun delete(call: ApplicationCall, emailRoute: EmailRoute.Delete)

}
