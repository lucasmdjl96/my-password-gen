package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.UserDto
import com.lucasmdjl.passwordgenerator.server.mapper.UserMapper
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import mu.KotlinLogging
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("UserMapperImpl")

object UserMapperImpl : UserMapper {

    override fun userToUserDto(user: User): UserDto = transaction {
        logger.debug { "userToUserDto call with user: $user" }
        user.load(User::emails)
        UserDto(
            user.username,
            user.emails.map(Email::emailAddress).toMutableList()
        )
    }


    override fun userIterableToUserDtoIterable(userList: Iterable<User>?): Iterable<UserDto>? {
        logger.debug { "userIterableToUserDtoIterable call with userList: $userList" }
        return userList?.map(UserMapperImpl::userToUserDto)
    }

}
