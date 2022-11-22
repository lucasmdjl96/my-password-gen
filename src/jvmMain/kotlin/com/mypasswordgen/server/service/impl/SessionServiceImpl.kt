/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.service.impl

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.idb.SessionIDBDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.SessionService
import com.mypasswordgen.server.service.UserService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SessionServiceImpl")

class SessionServiceImpl(
    private val userService: UserService,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val sessionMapper: SessionMapper
) : SessionService {

    override fun assignNew(oldSessionDto: SessionDto?) = transaction {
        logger.debug { "assignNew" }
        val newSession = sessionRepository.create()
        if (oldSessionDto != null) {
            val oldSession = find(oldSessionDto)
            if (oldSession != null) {
                val lastUser = sessionRepository.getLastUser(oldSession)
                if (lastUser != null) userRepository.setLastEmail(lastUser, null)
                moveAllUsers(oldSession, newSession)
                sessionRepository.delete(oldSession)
            }
        }
        with(sessionMapper) {
            newSession.toSessionDto()
        }
    }

    override fun find(sessionDto: SessionDto) = transaction {
        logger.debug { "find" }
        sessionRepository.getById(sessionDto.sessionId)
    }

    override fun delete(sessionDto: SessionDto): Unit? = transaction {
        logger.debug { "delete" }
        val session = find(sessionDto)
        if (session != null) sessionRepository.delete(session) else null
    }

    override fun moveAllUsers(fromSession: Session, toSession: Session) = transaction {
        logger.debug { "moveAllUsers" }
        userRepository.moveAll(fromSession.id.value, toSession.id.value)
    }

    override fun getFullSession(sessionDto: SessionDto): FullSessionClientDto = transaction {
        logger.debug { "getFullSession" }
        val session = find(sessionDto) ?: throw DataNotFoundException("No session found.")
        with(sessionMapper) {
            session.toFullSessionClientDto()
        }
    }

    override fun createFullSession(sessionDto: SessionDto, fullSession: FullSessionServerDto): Pair<SessionDto, SessionIDBDto> =
        transaction {
            logger.debug { "createFullSession" }
            val newSession = sessionRepository.create()
            delete(sessionDto)
            val sessionIDBDto = SessionIDBDto {
                for (fullUser in fullSession.users) {
                    +userService.createFullUser(fullUser, newSession.id.value)
                }
            }
            val newSessionDto = with(sessionMapper) {
                newSession.toSessionDto()
            }
            Pair(newSessionDto, sessionIDBDto)
        }


}
