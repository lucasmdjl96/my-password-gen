package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.dto.SessionDto
import io.ktor.server.application.*

interface SessionController {

    suspend fun put(call: ApplicationCall, sessionRoute: SessionRoute)

    fun validate(call: ApplicationCall, sessionDto: SessionDto): SessionDto?

    suspend fun challenge(call: ApplicationCall, sessionDto: SessionDto?)

}
