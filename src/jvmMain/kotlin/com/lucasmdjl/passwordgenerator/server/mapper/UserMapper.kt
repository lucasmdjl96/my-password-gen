package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.server.model.User

interface UserMapper {

    fun userToUserClientDto(user: User): UserClientDto

    fun User.toUserClientDto() = userToUserClientDto(this)

    fun usersToUserClientDtos(users: Iterable<User>): Iterable<UserClientDto>

    fun Iterable<User>.toUserClientDtos() = usersToUserClientDtos(this)


}
