package com.lucasmdjl.application.mapper

import com.lucasmdjl.application.model.User
import dto.UserDto

interface UserMapper {

    fun userToUserDto(user: User): UserDto

    fun userListToUserDtoList(userList: Iterable<User>?): Iterable<UserDto>?

}