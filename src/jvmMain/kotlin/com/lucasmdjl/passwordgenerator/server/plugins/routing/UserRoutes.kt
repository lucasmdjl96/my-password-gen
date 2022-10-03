package com.lucasmdjl.passwordgenerator.server.plugins.routing

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
        val username = call.receiveText().trim('"').encode()
        logger.debug {
            "/user/login: call with sessionId: $sessionId, username: $username"
        }
        val user = userService.getByName(username, sessionId)
        val userDto = if (user != null) {
            userMapper.userToUserDto(user)
        } else {
            null
        }
        logger.debug { "/user/login: respond with userDto: $userDto" }
        call.respondNullable(userDto)
    }
    post<UserRoute.Register> {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = call.receiveText().trim('"').encode()
        logger.debug {
            "/user/register: call with sessionId: $sessionId, username: $username"
        }
        val user = userService.create(username, sessionId)
        val userDto = if (user != null) {
            userMapper.userToUserDto(user)
        } else {
            null
        }
        logger.debug { "/user/register: respond with userDto: $userDto" }
        call.respondNullable(userDto)
    }
}
