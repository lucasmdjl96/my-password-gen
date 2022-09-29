package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.EmailRepository
import com.lucasmdjl.application.repository.impl.EmailRepositoryImpl
import com.lucasmdjl.application.service.EmailService
import org.jetbrains.exposed.sql.transactions.transaction


object EmailServiceImpl : EmailService {

    private val emailRepository: EmailRepository = EmailRepositoryImpl

    override fun addEmailToUser(emailAddress: String, user: User) = transaction {
        val id = emailRepository.createAndGetId(emailAddress, user)
        if (id != null) emailRepository.getById(id) else null
    }


    override fun getEmailFromUser(emailAddress: String, user: User) = transaction {
        emailRepository.getByAddressAndUser(emailAddress, user)
    }

    override fun removeEmailFromUser(emailAddress: String, user: User): Unit = transaction {
        emailRepository.delete(emailAddress, user)
    }


}