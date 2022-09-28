package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.repository.UserRepository
import com.lucasmdjl.application.repository.impl.UserRepositoryImpl
import com.lucasmdjl.application.service.UserService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object UserServiceImpl : UserService {

    private val userRepository: UserRepository = UserRepositoryImpl

    override fun create(username: String, sessionId: UUID) = transaction {
        val id = userRepository.create(username, sessionId)
        if (id != null) userRepository.getById(id) else null
    }


    override fun getByName(name: String, sessionId: UUID) = transaction {
        userRepository.getByNameAndSession(name, sessionId)
    }


    override fun moveAllUsers(fromSession: Session, toSession: Session) = transaction {
        userRepository.moveAll(fromSession, toSession)
    }

}