package com.mypasswordgen.server.mapper.impl

import com.mypasswordgen.common.dto.FullUserClientDto
import com.mypasswordgen.common.dto.client.UserClientDto
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
        FullUserClientDto {
            user.emails.forEach { email ->
                with(emailMapper) {
                    +email.toFullEmailClientDto()
                }
            }
        }
    }

}
