package com.lucasmdjl.application.mapper.impl


import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.mapper.SessionMapper
import com.lucasmdjl.application.model.Session
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SessionMapperImpl")

object SessionMapperImpl : SessionMapper {

    override fun sessionToSessionDto(session: Session): SessionDto {
        logger.debug { "sessionToSessionDto call with session: $session" }
        return SessionDto(session.id.value)
    }

    override fun sessionIterableToSessionDtoIterable(sessionList: Iterable<Session>?): Iterable<SessionDto>? {
        logger.debug { "sessionIterableToSessionDtoIterable call with sessionList: $sessionList" }
        return sessionList?.map(SessionMapperImpl::sessionToSessionDto)
    }

}