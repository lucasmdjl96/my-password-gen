/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.controller.impl

import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.server.controller.UserController
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

    override suspend fun get(call: ApplicationCall, userRoute: UserRoute.Login) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val userServerDto = UserServerDto(userRoute.username)
        val userClientDto = userService.find(userServerDto, sessionId)
        call.respond(userClientDto)
    }

    override suspend fun post(call: ApplicationCall, userRoute: UserRoute.Register) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val userServerDto = call.receive<UserServerDto>()
        val userClientDto = userService.create(userServerDto, sessionId)
        call.respond(userClientDto)
    }

    override suspend fun patch(call: ApplicationCall, userRoute: UserRoute.Logout) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val userServerDto = call.receive<UserServerDto>()
        userService.logout(userServerDto, sessionId)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun post(call: ApplicationCall, userRoute: UserRoute.Import) {
        val sessionDto = call.sessions.get<SessionDto>() ?: throw NotAuthenticatedException()
        val fullUserServerDto = call.receive<FullUserServerDto>()
        val userIDBDto = userService.createFullUser(fullUserServerDto, sessionDto.sessionId)
        call.respond(userIDBDto)
    }

    override suspend fun get(call: ApplicationCall, userRoute: UserRoute.Export) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val userServerDto = UserServerDto(userRoute.username)
        val fullUserClientDto = userService.getFullUser(userServerDto, sessionId)
        call.respond(fullUserClientDto)
    }

}
