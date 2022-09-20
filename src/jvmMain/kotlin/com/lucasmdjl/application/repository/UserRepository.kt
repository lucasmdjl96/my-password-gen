package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.User

interface UserRepository {

    fun create(userName: String): User

    fun getByName(userName: String): User?

}