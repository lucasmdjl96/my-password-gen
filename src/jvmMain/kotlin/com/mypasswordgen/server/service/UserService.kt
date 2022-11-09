package com.mypasswordgen.server.service

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.server.UserServerDto
import java.util.*

interface UserService {

    fun create(userServerDto: UserServerDto, sessionId: UUID): UserClientDto

    fun find(userServerDto: UserServerDto, sessionId: UUID): UserClientDto

    fun logout(userServerDto: UserServerDto, sessionId: UUID)

}
