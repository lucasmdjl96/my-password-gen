package com.lucasmdjl.passwordgenerator.server.mapper.impl


import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.SessionMapper
import com.lucasmdjl.passwordgenerator.server.model.Session
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SessionMapperImpl")

class SessionMapperImpl : SessionMapper {

    override fun sessionToSessionDto(session: Session): SessionDto {
        logger.debug { "sessionToSessionDto" }
        return SessionDto(session.id.value)
    }

}
