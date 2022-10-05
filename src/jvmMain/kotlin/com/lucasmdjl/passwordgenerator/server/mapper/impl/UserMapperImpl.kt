package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.UserMapper
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("UserMapperImpl")

object UserMapperImpl : UserMapper {

    override fun userToUserClientDto(user: User): UserClientDto = transaction {
        logger.debug { "userToUserDto call with user: $user" }
        user.load(User::emails)
        UserClientDto(
            user.username,
            user.emails.map(Email::emailAddress).toMutableList()
        )
    }


    override fun userIterableToUserClientDtoIterable(userList: Iterable<User>?): Iterable<UserClientDto>? {
        logger.debug { "userIterableToUserDtoIterable call with userList: $userList" }
        return userList?.map(UserMapperImpl::userToUserClientDto)
    }

}
