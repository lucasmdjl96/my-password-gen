package com.lucasmdjl.application.mapper

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.model.Session

interface SessionMapper {

    fun sessionToSessionDto(session: Session): SessionDto

    fun sessionListToSessionDtoList(sessionList: Iterable<Session>?): Iterable<SessionDto>?

}