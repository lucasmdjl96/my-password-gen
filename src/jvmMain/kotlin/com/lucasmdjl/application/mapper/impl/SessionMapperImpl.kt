package com.lucasmdjl.application.mapper.impl


import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.mapper.SessionMapper
import com.lucasmdjl.application.model.Session

object SessionMapperImpl : SessionMapper {

    override fun sessionToSessionDto(session: Session): SessionDto {
        return SessionDto(session.id.value)
    }

    override fun sessionListToSessionDtoList(sessionList: Iterable<Session>?): Iterable<SessionDto>? {
        return sessionList?.map(SessionMapperImpl::sessionToSessionDto)
    }

}