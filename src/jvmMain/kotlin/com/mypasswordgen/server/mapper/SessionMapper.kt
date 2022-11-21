package com.mypasswordgen.server.mapper

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.model.Session

interface SessionMapper {

    fun sessionToSessionDto(session: Session): SessionDto

    fun Session.toSessionDto() = sessionToSessionDto(this)

    fun sessionToFullSessionClientDto(session: Session): FullSessionClientDto

    fun Session.toFullSessionClientDto() = sessionToFullSessionClientDto(this)

}
