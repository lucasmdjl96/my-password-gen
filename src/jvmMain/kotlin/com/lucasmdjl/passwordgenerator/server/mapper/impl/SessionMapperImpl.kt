package com.lucasmdjl.passwordgenerator.server.mapper.impl


import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.SessionMapper
import com.lucasmdjl.passwordgenerator.server.model.Session
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SessionMapperImpl")

object SessionMapperImpl : SessionMapper {

    override fun sessionToSessionDto(session: Session): SessionDto {
        logger.debug { "sessionToSessionDto call with session: $session" }
        return SessionDto(session.id.value)
    }

    override fun sessionIterableToSessionDtoIterable(sessionIterable: Iterable<Session>): Iterable<SessionDto> {
        logger.debug { "sessionIterableToSessionDtoIterable call with sessionList: $sessionIterable" }
        return sessionIterable.map(SessionMapperImpl::sessionToSessionDto)
    }

}
