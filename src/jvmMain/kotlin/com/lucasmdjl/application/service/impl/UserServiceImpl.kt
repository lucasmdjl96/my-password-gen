package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.mapper.UserMapper
import com.lucasmdjl.application.mapper.impl.UserMapperImpl
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.SessionRepository
import com.lucasmdjl.application.repository.UserRepository
import com.lucasmdjl.application.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.application.repository.impl.UserRepositoryImpl
import com.lucasmdjl.application.service.UserService
import dto.UserDto
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object UserServiceImpl : UserService {

    private val sessionRepository: SessionRepository = SessionRepositoryImpl

    private val userRepository: UserRepository = UserRepositoryImpl

    private val userMapper: UserMapper = UserMapperImpl

    override fun create(username: String, sessionId: UUID): UserDto? =
        transaction {
            val session = sessionRepository.getById(sessionId)!!
            val user = if (userRepository.getByNameAndSession(username, session) == null) {
                userRepository.create(username, session)
            } else null
            if (user != null) userMapper.userToUserDto(user) else null
        }


    override fun getByName(name: String, sessionId: UUID): UserDto? =
        transaction {
            val session = sessionRepository.getById(sessionId)!!
            val user = userRepository.getByNameAndSession(name, session)?.load(User::emails)
            if (user != null) userMapper.userToUserDto(user) else null
        }


    override fun moveAllUsers(fromSessionId: UUID, toSessionId: UUID) =
        transaction {
            val fromSession = sessionRepository.getById(fromSessionId)!!
            val toSession = sessionRepository.getById(toSessionId)!!
            userRepository.moveAll(fromSession, toSession)
        }

}