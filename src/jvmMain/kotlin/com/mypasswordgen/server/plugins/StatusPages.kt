/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<DataNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message)
        }
        exception<DataConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message)
        }
        exception<NotAuthenticatedException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, cause.message)
        }
        exception<NotEnoughInformationException> { call, cause ->
            call.respond(HttpStatusCode.PreconditionFailed, cause.message)
        }
    }
}

class DataNotFoundException(override val message: String = "") : RuntimeException()

class DataConflictException(override val message: String = "") : RuntimeException()

class NotAuthenticatedException(override val message: String = "Session not found.") : RuntimeException()

class NotEnoughInformationException(override val message: String = "") : RuntimeException()
