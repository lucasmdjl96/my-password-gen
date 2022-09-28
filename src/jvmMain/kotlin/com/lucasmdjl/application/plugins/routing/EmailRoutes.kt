package com.lucasmdjl.application.plugins.routing

import com.lucasmdjl.application.dto.SessionCookie
import com.lucasmdjl.application.emailMapper
import com.lucasmdjl.application.emailService
import com.lucasmdjl.application.userMapper
import com.lucasmdjl.application.userService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.util.*

fun Route.emailRoutes() {
    route("/email") {
        post("/new") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.receiveText().trim('"')
            emailService.addEmailToUser(emailAddress, user)
            val userDto = userMapper.userToUserDto(user)
            call.respond(userDto)
        }
        get("/find/{emailAddress}") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.parameters.getOrFail("emailAddress")
            val email = emailService.getEmailFromUser(emailAddress, user)
            val emailDto = if (email != null) {
                emailMapper.emailToEmailDto(email)
            } else {
                null
            }
            call.respondNullable(emailDto)
        }
        delete("/delete/{emailAddress}") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.parameters.getOrFail("emailAddress")
            emailService.removeEmailFromUser(emailAddress, user)
            val userDto = userMapper.userToUserDto(user)
            call.respond(userDto)
        }
    }
}