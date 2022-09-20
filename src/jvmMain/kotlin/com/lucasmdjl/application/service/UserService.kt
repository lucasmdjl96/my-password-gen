package com.lucasmdjl.application.service

import dto.UserDto

interface UserService {

    fun create(username: String): UserDto?

    fun getByName(name: String): UserDto?

}