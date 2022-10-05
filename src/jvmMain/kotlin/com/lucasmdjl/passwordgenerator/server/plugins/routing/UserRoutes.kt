package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.userMapper
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
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = call.receive<UserServerDto>().username.encode()
        val user = userService.getByName(username, sessionId)
        val userClientDto = if (user != null) {
            userMapper.userToUserClientDto(user)
        } else {
            null
        }
        call.respondNullable(userClientDto)
    }
    post<UserRoute.Register> {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = call.receive<UserServerDto>().username.encode()
        val user = userService.create(username, sessionId)
        val userClientDto = if (user != null) {
            userMapper.userToUserClientDto(user)
        } else {
            null
        }
        call.respondNullable(userClientDto)
    }
}
