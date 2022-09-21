package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.model.User

interface UserRepository {

    fun create(username: String, session: Session): User

    fun getByName(username: String, session: Session): User?

}