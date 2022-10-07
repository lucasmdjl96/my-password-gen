package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session

interface SessionMapper {

    fun sessionToSessionDto(session: Session): SessionDto

    fun Session.toSessionDto() = sessionToSessionDto(this)

    fun sessionsToSessionDtos(sessions: Iterable<Session>): Iterable<SessionDto>

    fun Iterable<Session>.toSessionDtos() = sessionsToSessionDtos(this)

}
