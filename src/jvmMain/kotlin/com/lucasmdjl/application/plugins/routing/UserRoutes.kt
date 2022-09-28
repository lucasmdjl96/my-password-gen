package com.lucasmdjl.application.plugins.routing

import com.lucasmdjl.application.crypto.encode
import com.lucasmdjl.application.dto.SessionCookie
import com.lucasmdjl.application.userMapper
import com.lucasmdjl.application.userService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Route.userRoutes() {
    route("/user") {
        post("/login") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.receiveText().trim('"').encode()
            val user = userService.getByName(username, sessionId)
            val userDto = if (user != null) {
                userMapper.userToUserDto(user)
            } else {
                null
            }
            call.respondNullable(userDto)
        }
        post("/register") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.receiveText().trim('"').encode()
            val user = userService.create(username, sessionId)
            val userDto = if (user != null) {
                userMapper.userToUserDto(user)
            } else {
                null
            }
            call.respondNullable(userDto)
        }
    }
}