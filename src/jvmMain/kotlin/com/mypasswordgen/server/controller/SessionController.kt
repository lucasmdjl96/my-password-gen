/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.dto.SessionDto
import io.ktor.server.application.*

interface SessionController {

    suspend fun put(call: ApplicationCall, sessionRoute: SessionRoute.Update)
    suspend fun get(call: ApplicationCall, sessionRoute: SessionRoute.Export)
    suspend fun post(call: ApplicationCall, sessionRoute: SessionRoute.Import)

    fun validate(call: ApplicationCall, sessionDto: SessionDto): SessionDto?

    suspend fun challenge(call: ApplicationCall, sessionDto: SessionDto?)

}
