package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.model.Email
import java.util.*

interface EmailService {

    fun create(emailAddress: String, username: String, sessionId: UUID): Email?

    fun find(emailAddress: String, username: String, sessionId: UUID): Email?

    fun delete(emailAddress: String, username: String, sessionId: UUID): Unit?

}
