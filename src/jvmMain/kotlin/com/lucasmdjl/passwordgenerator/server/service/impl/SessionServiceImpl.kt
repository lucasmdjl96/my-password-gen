package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.sessionRepository
import com.lucasmdjl.passwordgenerator.server.userService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object SessionServiceImpl : SessionService {

    override fun assignNew(oldSessionDto: SessionDto?) = transaction {
        val newSession = sessionRepository.create()
        if (oldSessionDto != null) {
            val oldSession = sessionRepository.getById(oldSessionDto.sessionId)
            if (oldSession != null) {
                userService.moveAllUsers(oldSession, newSession)
                sessionRepository.delete(oldSession)
            }
        }
        newSession
    }

    override fun getById(sessionId: UUID) = transaction {
        sessionRepository.getById(sessionId)
    }

}
