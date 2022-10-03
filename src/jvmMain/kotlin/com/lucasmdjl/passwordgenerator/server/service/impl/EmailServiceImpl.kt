package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.EmailRepository
import com.lucasmdjl.passwordgenerator.server.repository.impl.EmailRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("EmailServiceImpl")

object EmailServiceImpl : EmailService {

    private val emailRepository: EmailRepository = EmailRepositoryImpl

    override fun addEmailToUser(emailAddress: String, user: User) = transaction {
        logger.debug { "addEmailToUser call with emailAddress: $emailAddress, user: $user" }
        val id = emailRepository.createAndGetId(emailAddress, user)
        if (id != null) emailRepository.getById(id) else null
    }

    override fun getEmailFromUser(emailAddress: String, user: User) = transaction {
        logger.debug { "getEmailFromUser call with emailAddress: $emailAddress, user: $user" }
        emailRepository.getByAddressAndUser(emailAddress, user)
    }

    override fun removeEmailFromUser(emailAddress: String, user: User): Unit = transaction {
        logger.debug { "removeEmailFromUser call with emailAddress: $emailAddress, user: $user" }
        emailRepository.delete(emailAddress, user)
    }


}