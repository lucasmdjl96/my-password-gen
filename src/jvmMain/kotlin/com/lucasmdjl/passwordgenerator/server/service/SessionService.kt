package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session

interface SessionService {

    fun assignNew(oldSessionDto: SessionDto?): Session

    fun find(sessionDto: SessionDto): Session?

    fun delete(sessionDto: SessionDto): Unit

}
