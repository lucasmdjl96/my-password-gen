package com.mypasswordgen.server.mapper

import com.mypasswordgen.common.dto.FullUserClientDto
import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.server.model.User

interface UserMapper {

    fun userToUserClientDto(user: User): UserClientDto

    fun User.toUserClientDto() = userToUserClientDto(this)

    fun userToFullUserClientDto(user: User): FullUserClientDto

    fun User.toFullUserClientDto() = userToFullUserClientDto(this)

}
