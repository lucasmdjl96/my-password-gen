package com.lucasmdjl.application.service

import dto.EmailDto
import dto.UserDto
import java.util.*

interface EmailService {

    fun addEmailToUser(emailAddress: String, username: String, sessionId: UUID): UserDto

    fun getEmailFromUser(emailAddress: String, username: String, sessionId: UUID): EmailDto?

    fun removeEmailFromUser(emailAddress: String, username: String, sessionId: UUID): UserDto

}