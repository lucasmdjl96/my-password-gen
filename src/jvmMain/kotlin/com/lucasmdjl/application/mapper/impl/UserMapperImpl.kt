package com.lucasmdjl.application.mapper.impl

import com.lucasmdjl.application.mapper.UserMapper
import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User
import dto.UserDto
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