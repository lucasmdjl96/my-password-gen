package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.model.User
import java.util.*

interface UserService {

    fun create(userServerDto: UserServerDto, sessionId: UUID): User

    fun find(userServerDto: UserServerDto, sessionId: UUID): User

    fun logout(userServerDto: UserServerDto, sessionId: UUID)

}
