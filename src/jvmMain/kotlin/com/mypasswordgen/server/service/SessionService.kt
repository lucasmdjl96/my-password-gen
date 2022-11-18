package com.mypasswordgen.server.service

import com.mypasswordgen.common.dto.FullSessionClientDto
import com.mypasswordgen.common.dto.FullSessionServerDto
import com.mypasswordgen.common.dto.SessionIDBDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.model.Session

interface SessionService {

    fun assignNew(oldSessionDto: SessionDto?): SessionDto

    fun find(sessionDto: SessionDto): Session?

    fun delete(sessionDto: SessionDto): Unit?

    fun moveAllUsers(fromSession: Session, toSession: Session)

    fun getFullSession(sessionDto: SessionDto): FullSessionClientDto

    fun createFullSession(sessionDto: SessionDto, fullSession: FullSessionServerDto): Pair<SessionDto, SessionIDBDto>

}
