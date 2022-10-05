package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session

interface SessionMapper {

    fun sessionToSessionDto(session: Session): SessionDto

    fun Session.toSessionDto() = sessionToSessionDto(this)

    fun sessionIterableToSessionDtoIterable(sessionIterable: Iterable<Session>): Iterable<SessionDto>

    fun Iterable<Session>.toSessionDtoIterable() = sessionIterableToSessionDtoIterable(this)

}
