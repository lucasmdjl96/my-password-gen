package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.dto.SessionDto
import io.ktor.server.application.*

interface SessionController {

    suspend fun put(call: ApplicationCall, sessionRoute: SessionRoute.Update)
    suspend fun get(call: ApplicationCall, sessionRoute: SessionRoute.Export)
    suspend fun post(call: ApplicationCall, sessionRoute: SessionRoute.Import)

    fun validate(call: ApplicationCall, sessionDto: SessionDto): SessionDto?

    suspend fun challenge(call: ApplicationCall, sessionDto: SessionDto?)

}
