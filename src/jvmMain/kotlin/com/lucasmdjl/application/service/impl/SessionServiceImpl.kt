package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.mapper.SessionMapper
import com.lucasmdjl.application.mapper.impl.SessionMapperImpl
import com.lucasmdjl.application.repository.SessionRepository
import com.lucasmdjl.application.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.application.service.SessionService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object SessionServiceImpl : SessionService {

    private val sessionRepository: SessionRepository = SessionRepositoryImpl

    private val sessionMapper: SessionMapper = SessionMapperImpl

    override fun create(): SessionDto =
        transaction {
            val session = sessionRepository.create()
            sessionMapper.sessionToSessionDto(session)
        }

    override fun getById(sessionId: UUID): SessionDto? =
        transaction {
            val session = sessionRepository.getById(sessionId)
            if (session != null) sessionMapper.sessionToSessionDto(session) else null
        }

}