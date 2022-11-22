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

import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.EmailService
import com.mypasswordgen.server.service.UserService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("UserServiceImpl")

class UserServiceImpl(
    private val emailService: EmailService,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val userMapper: UserMapper
) :
    UserService {

    override fun create(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val username = userServerDto.username
        if (userRepository.getByNameAndSession(username, sessionId) != null) throw DataConflictException("Username already exists.")
        val id = userRepository.createAndGetId(username, sessionId)
        val user = userRepository.getById(id)!!
        sessionRepository.setLastUser(sessionId, user)
        with(userMapper) {
            user.toUserClientDto()
        }
    }

    override fun find(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val username = userServerDto.username
        val user = userRepository.getByNameAndSession(username, sessionId) ?: throw DataNotFoundException("No such username found.")
        sessionRepository.setLastUser(sessionId, user)
        with(userMapper) {
            user.toUserClientDto()
        }
    }

    override fun logout(userServerDto: UserServerDto, sessionId: UUID): Unit = transaction {
        logger.debug { "logout" }
        val user = sessionRepository.getIfLastUser(sessionId, userServerDto.username) ?: throw DataNotFoundException("Username is not from last user.")
        userRepository.setLastEmail(user, null)
        sessionRepository.setLastUser(sessionId, null)
    }

    override fun createFullUser(fullUser: FullUserServerDto, sessionId: UUID) = transaction {
        logger.debug { "createFullUser" }
        val username = fullUser.username
        if (userRepository.getByNameAndSession(username, sessionId) != null) throw DataConflictException("Import failed. Username already exists.")
        val id = userRepository.createAndGetId(username, sessionId)
        UserIDBDto {
            for (fullEmail in fullUser.emails) {
                +emailService.createFullEmail(fullEmail, id)
            }
        }

    }

    override fun getFullUser(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "getFullUser" }
        val username = userServerDto.username
        val user = userRepository.getByNameAndSession(username, sessionId) ?: throw DataNotFoundException("No such username found.")
        with(userMapper) {
            user.toFullUserClientDto()
        }
    }

}
