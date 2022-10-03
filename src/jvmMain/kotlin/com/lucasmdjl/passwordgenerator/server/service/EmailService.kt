package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User

interface EmailService {

    fun addEmailToUser(emailAddress: String, user: User): Email?

    fun getEmailFromUser(emailAddress: String, user: User): Email?

    fun removeEmailFromUser(emailAddress: String, user: User): Unit?

}