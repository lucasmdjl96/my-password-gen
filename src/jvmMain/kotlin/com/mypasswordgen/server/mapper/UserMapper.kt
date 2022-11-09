package com.mypasswordgen.server.mapper

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.server.model.User

interface UserMapper {

    fun userToUserClientDto(user: User): UserClientDto

    fun User.toUserClientDto() = userToUserClientDto(this)

}
