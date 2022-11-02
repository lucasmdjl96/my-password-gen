package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.controller.UserController
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.plugins.NotAuthenticatedException
import com.lucasmdjl.passwordgenerator.server.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class UserControllerImpl(
    private val userService: UserService
) : UserController {

    override suspend fun post(call: ApplicationCall, userRoute: UserRoute.Login) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val userServerDto = call.receive<UserServerDto>().encode()
        val userClientDto = userService.find(userServerDto, sessionId)
        call.respond(userClientDto)
    }

    override suspend fun post(call: ApplicationCall, userRoute: UserRoute.Register) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val userServerDto = call.receive<UserServerDto>().encode()
        val userClientDto = userService.create(userServerDto, sessionId)
        call.respond(userClientDto)
    }

    override suspend fun patch(call: ApplicationCall, userRoute: UserRoute.Logout) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val userServerDto = call.receive<UserServerDto>().encode()
        userService.logout(userServerDto, sessionId)
        call.respond(HttpStatusCode.OK)
    }

}
