package com.lucasmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import io.ktor.server.application.*

interface SessionController {

    suspend fun put(call: ApplicationCall, sessionRoute: SessionRoute)

    fun validate(call: ApplicationCall, sessionDto: SessionDto): SessionDto?

    suspend fun challenge(call: ApplicationCall, sessionDto: SessionDto?)

}
