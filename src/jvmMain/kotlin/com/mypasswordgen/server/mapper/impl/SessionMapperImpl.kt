package com.mypasswordgen.server.mapper.impl


import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.model.Session
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SessionMapperImpl")

class SessionMapperImpl : SessionMapper {

    override fun sessionToSessionDto(session: Session): SessionDto {
        logger.debug { "sessionToSessionDto" }
        return SessionDto(session.id.value)
    }

}
