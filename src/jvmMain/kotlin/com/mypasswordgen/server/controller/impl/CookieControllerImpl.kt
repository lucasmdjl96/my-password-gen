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

import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.server.controller.CookieController
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.service.SessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class CookieControllerImpl(private val sessionService: SessionService) :
    CookieController {

    override suspend fun get(call: ApplicationCall, cookieRoute: CookieRoute.OptOut) {
        val sessionDto = call.sessions.get<SessionDto>()
        if (sessionDto != null) {
            sessionService.delete(sessionDto)
            call.sessions.clear<SessionDto>()
        }
        //call.response.header("Clear-Site-Data", "\"*\"")
        call.respondText(
            this::class.java.classLoader.getResource("html/opt-out.html")!!.readText(),
            ContentType.Text.Html
        )
    }

    override suspend fun get(call: ApplicationCall, cookieRoute: CookieRoute.Policy) {
        call.respondText(
            this::class.java.classLoader.getResource("html/policy.html")!!.readText(),
            ContentType.Text.Html
        )
    }

}
