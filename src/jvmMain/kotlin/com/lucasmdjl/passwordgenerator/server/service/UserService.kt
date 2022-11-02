package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import java.util.*

interface UserService {

    fun create(userServerDto: UserServerDto, sessionId: UUID): UserClientDto

    fun find(userServerDto: UserServerDto, sessionId: UUID): UserClientDto

    fun logout(userServerDto: UserServerDto, sessionId: UUID)

}
