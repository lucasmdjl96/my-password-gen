package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.server.emailRepository
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import com.lucasmdjl.passwordgenerator.server.userService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object EmailServiceImpl : EmailService {

    override fun create(emailAddress: String, username: String, sessionId: UUID) = transaction {
        val user = userService.find(username, sessionId)!!
        val id = emailRepository.createAndGetId(emailAddress, user)
        if (id != null) emailRepository.getById(id) else null
    }

    override fun find(emailAddress: String, username: String, sessionId: UUID) = transaction {
        val user = userService.find(username, sessionId)!!
        emailRepository.getByAddressAndUser(emailAddress, user)
    }

    override fun delete(emailAddress: String, username: String, sessionId: UUID) = transaction {
        val user = userService.find(username, sessionId)!!
        emailRepository.delete(emailAddress, user)
    }
}
