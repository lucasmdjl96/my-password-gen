package com.lucasmdjl.application.mapper.impl

import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.UserMapper
import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User
import dto.UserDto
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

object UserMapperImpl : UserMapper {

    private val emailMapper: EmailMapper = EmailMapperImpl

    override fun userToUserDto(user: User): UserDto = transaction {
        user.load(User::emails)
        UserDto(
            user.username,
            user.emails.map(Email::emailAddress).toMutableList()
        )
    }


    override fun userIterableToUserDtoIterable(userList: Iterable<User>?): Iterable<UserDto>? {
        return userList?.map(UserMapperImpl::userToUserDto)
    }

}