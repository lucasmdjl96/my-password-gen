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

import com.mypasswordgen.server.dto.SessionDto
import io.ktor.server.application.*

import io.ktor.server.sessions.*

private const val cookieDuration: Long = (366 * 24 + 6) * 60 * 60 // 365.25 and 1 days

fun Application.installSessions() {
    pluginLogger.debug { "Installing Sessions" }
    install(Sessions) {
        cookie<SessionDto>("session") {
            cookie.maxAgeInSeconds = cookieDuration
            cookie.secure = true
            cookie.path = "/"
            cookie.httpOnly = true
        }
    }
}
