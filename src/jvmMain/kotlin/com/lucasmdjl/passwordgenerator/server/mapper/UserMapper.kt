package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.common.dto.UserDto
import com.lucasmdjl.passwordgenerator.server.model.User

interface UserMapper {

    fun userToUserDto(user: User): UserDto

    fun userIterableToUserDtoIterable(userList: Iterable<User>?): Iterable<UserDto>?

}
