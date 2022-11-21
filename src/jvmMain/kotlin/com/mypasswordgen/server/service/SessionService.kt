package com.mypasswordgen.server.service

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.idb.SessionIDBDto
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
