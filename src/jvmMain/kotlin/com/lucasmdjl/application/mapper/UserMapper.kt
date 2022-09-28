package com.lucasmdjl.application.mapper

import com.lucasmdjl.application.model.User
import dto.UserDto

interface UserMapper {

    fun userToUserDto(user: User): UserDto

    fun userIterableToUserDtoIterable(userList: Iterable<User>?): Iterable<UserDto>?

}