package com.lucasmdjl.application.service

import dto.UserDto
import java.util.*

interface UserService {

    fun create(username: String, sessionId: UUID): UserDto?

    fun getByName(name: String, sessionId: UUID): UserDto?

    fun moveAllUsers(fromSessionId: UUID, toSessionId: UUID)

}