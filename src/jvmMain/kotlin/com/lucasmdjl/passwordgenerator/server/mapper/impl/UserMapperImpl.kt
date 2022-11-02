package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.UserMapper
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("UserMapperImpl")

class UserMapperImpl : UserMapper {

    override fun userToUserClientDto(user: User): UserClientDto = transaction {
        logger.debug { "userToUserClientDto: $user" }
        user.load(User::emails)
        UserClientDto(
            user.username,
            user.emails.map { email -> email.id.value.toString() }.toMutableList()
        )
    }

    override fun loadEmailIdsFrom(user: User): MutableList<String> = transaction {
        user.load(User::emails)
        user.emails.map { email -> email.id.value.toString() }.toMutableList()
    }

}
