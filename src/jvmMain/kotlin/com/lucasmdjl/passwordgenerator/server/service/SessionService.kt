package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session
import java.util.*

interface SessionService {

    fun assignNew(oldSessionDto: SessionDto?): Session

    fun getById(sessionId: UUID): Session?

}
