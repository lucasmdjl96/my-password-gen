/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.mapper.impl


import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.model.Session
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SessionMapperImpl")

class SessionMapperImpl(private val userMapper: UserMapper) : SessionMapper {

    override fun sessionToSessionDto(session: Session): SessionDto {
        logger.debug { "sessionToSessionDto" }
        return SessionDto(session.id.value)
    }

    override fun sessionToFullSessionClientDto(session: Session) = transaction {
        logger.debug { "sessionToFullSessionClientDto" }
        FullSessionClientDto {
            session.users.forEach { user ->
                with(userMapper) {
                    +user.toFullUserClientDto()
                }
            }
        }
    }

}
