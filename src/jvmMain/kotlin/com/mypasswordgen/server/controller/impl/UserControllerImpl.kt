package com.mypasswordgen.server.controller.impl

import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.server.controller.UserController
import com.mypasswordgen.server.crypto.encode
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.plugins.NotAuthenticatedException
import com.mypasswordgen.server.service.UserService
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
