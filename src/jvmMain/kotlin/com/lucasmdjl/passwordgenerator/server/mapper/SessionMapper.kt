package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session

interface SessionMapper {

    fun sessionToSessionDto(session: Session): SessionDto

    fun sessionIterableToSessionDtoIterable(sessionList: Iterable<Session>?): Iterable<SessionDto>?

}