package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.mapper.UserMapper
import com.lucasmdjl.application.mapper.impl.UserMapperImpl
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.UserRepository
import com.lucasmdjl.application.repository.impl.UserRepositoryImpl
import com.lucasmdjl.application.service.UserService
import dto.UserDto
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

object UserServiceImpl : UserService {

    private val userRepository: UserRepository = UserRepositoryImpl

    private val userMapper: UserMapper = UserMapperImpl

    override fun create(username: String): UserDto? =
        transaction {
            val user = if (userRepository.getByName(username) == null) {
                userRepository.create(username)
            } else null
            if (user != null) userMapper.userToUserDto(user) else null
        }



    override fun getByName(name: String): UserDto? =
        transaction {
            val user = userRepository.getByName(name)?.load(User::emails)
            if (user != null) userMapper.userToUserDto(user) else null
        }


}