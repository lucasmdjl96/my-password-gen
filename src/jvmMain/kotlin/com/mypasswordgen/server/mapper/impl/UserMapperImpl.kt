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

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("UserMapperImpl")

class UserMapperImpl(private val emailMapper: EmailMapper) : UserMapper {

    override fun userToUserClientDto(user: User): UserClientDto = transaction {
        logger.debug { "userToUserClientDto" }
        UserClientDto(user.id.value.toString(), user.emails.map { email -> email.id.value.toString() })
    }

    override fun userToFullUserClientDto(user: User) = transaction {
        logger.debug { "userToFullUserClientDto" }
        FullUserClientDto(user.id.value.toString()) {
            user.emails.forEach { email ->
                with(emailMapper) {
                    +email.toFullEmailClientDto()
                }
            }
        }
    }

}
