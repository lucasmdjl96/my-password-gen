package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.impl.UserMapperImpl.toUserClientDto
import com.lucasmdjl.passwordgenerator.server.userService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("UserRoutes")

fun Route.userRoutes() {
    post<UserRoute.Login> {
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val userServerDto = call.receive<UserServerDto>().encode()
        val userClientDto = userService.find(userServerDto, sessionId)?.toUserClientDto()
        call.respondNullable(userClientDto)
    }
    post<UserRoute.Register> {
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val userServerDto = call.receive<UserServerDto>().encode()
        val userClientDto = userService.create(userServerDto, sessionId)?.toUserClientDto()
        call.respondNullable(userClientDto)
    }
}
