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

import com.mypasswordgen.server.controller.SessionController
import com.mypasswordgen.server.dto.SessionDto
import io.ktor.server.application.*
import io.ktor.server.auth.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("Authentication")

fun Application.installAuthentication() {

    val sessionController by inject<SessionController>()

    pluginLogger.debug { "Installing Authentication" }
    install(Authentication) {
        session<SessionDto>("session-auth") {
            validate { sessionDto ->
                logger.debug { "validate" }
                sessionController.validate(this, sessionDto)
            }
            challenge { sessionDto ->
                logger.debug { "challenge" }
                sessionController.challenge(call, sessionDto)
            }
        }
    }
}
