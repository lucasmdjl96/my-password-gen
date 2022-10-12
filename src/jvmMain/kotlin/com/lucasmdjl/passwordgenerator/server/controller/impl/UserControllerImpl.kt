package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.controller.UserController
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.UserMapper
import com.lucasmdjl.passwordgenerator.server.service.UserService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class UserControllerImpl(
    private val userService: UserService,
    private val userMapper: UserMapper
) : UserController {

    override suspend fun post(call: ApplicationCall, userRoute: UserRoute.Login) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val userServerDto = call.receive<UserServerDto>().encode()
        val userClientDto = with(userMapper) {
            userService.find(userServerDto, sessionId)?.toUserClientDto()
        }
        call.respondNullable(userClientDto)
    }

    override suspend fun post(call: ApplicationCall, userRoute: UserRoute.Register) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val userServerDto = call.receive<UserServerDto>().encode()
        val userClientDto = with(userMapper) {
            userService.create(userServerDto, sessionId)?.toUserClientDto()
        }
        call.respondNullable(userClientDto)
    }

}
