package com.lucasmdjl.application.service

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User

interface EmailService {

    fun addEmailToUser(emailAddress: String, user: User): Email?

    fun getEmailFromUser(emailAddress: String, user: User): Email?

    fun removeEmailFromUser(emailAddress: String, user: User): Unit?

}