package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.sessionRepository
import com.lucasmdjl.passwordgenerator.server.userService
import org.jetbrains.exposed.sql.transactions.transaction

object SessionServiceImpl : SessionService {

    override fun assignNew(oldSessionDto: SessionDto?) = transaction {
        val newSession = sessionRepository.create()
        if (oldSessionDto != null) {
            val oldSession = find(oldSessionDto)
            if (oldSession != null) {
                userService.moveAllUsers(oldSession, newSession)
                sessionRepository.delete(oldSession)
            }
        }
        newSession
    }

    override fun find(sessionDto: SessionDto) = transaction {
        sessionRepository.getById(sessionDto.sessionId)
    }

}
