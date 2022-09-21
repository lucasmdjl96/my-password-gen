package com.lucasmdjl.application.mapper.impl


import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.SessionMapper
import com.lucasmdjl.application.model.Session

object SessionMapperImpl : SessionMapper {

    private val emailMapper : EmailMapper = EmailMapperImpl

    override fun sessionToSessionDto(session: Session): SessionDto {
        return SessionDto(session.id.value, session.password)
    }

    override fun sessionListToSessionDtoList(sessionList: Iterable<Session>?): Iterable<SessionDto>? {
        return sessionList?.map(SessionMapperImpl::sessionToSessionDto)
    }

}