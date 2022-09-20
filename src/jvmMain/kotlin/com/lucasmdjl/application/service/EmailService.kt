package com.lucasmdjl.application.service

import dto.EmailDto
import dto.UserDto

interface EmailService {

    fun addEmailToUser(emailAddress: String, username: String): UserDto

    fun getEmailFromUser(emailAddress: String, username: String): EmailDto?

}