package com.mypasswordgen.server.mapper.impl

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("UserMapperImpl")

class UserMapperImpl : UserMapper {

    override fun userToUserClientDto(user: User): UserClientDto = transaction {
        logger.debug { "userToUserClientDto: $user" }
        user.load(User::emails)
        UserClientDto(
            user.id.value.toString(),
            user.emails.map { email -> email.id.value.toString() }
        )
    }

}