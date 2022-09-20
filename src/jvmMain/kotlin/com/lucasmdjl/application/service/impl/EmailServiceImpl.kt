package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.UserMapper
import com.lucasmdjl.application.mapper.impl.EmailMapperImpl
import com.lucasmdjl.application.mapper.impl.UserMapperImpl
import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.EmailRepository
import com.lucasmdjl.application.repository.UserRepository
import com.lucasmdjl.application.repository.impl.EmailRepositoryImpl
import com.lucasmdjl.application.repository.impl.UserRepositoryImpl
import com.lucasmdjl.application.service.EmailService
import dto.EmailDto
import dto.UserDto
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction


object EmailServiceImpl : EmailService {

    private val userRepository: UserRepository = UserRepositoryImpl

    private val userMapper: UserMapper = UserMapperImpl

    private val emailRepository: EmailRepository = EmailRepositoryImpl

    private val emailMapper: EmailMapper = EmailMapperImpl

    override fun addEmailToUser(emailAddress: String, username: String): UserDto =
        transaction {
            val user = userRepository.getByName(username)!!
            emailRepository.create(emailAddress, user)
            user.load(User::emails)
            userMapper.userToUserDto(user)
        }


    override fun getEmailFromUser(emailAddress: String, username: String): EmailDto? =
        transaction {
            val user = userRepository.getByName(username)!!
            val email = emailRepository.getByAddressAndUser(emailAddress, user)?.load(Email::sites)
            if (email != null) emailMapper.emailToEmailDto(email) else null
        }


}