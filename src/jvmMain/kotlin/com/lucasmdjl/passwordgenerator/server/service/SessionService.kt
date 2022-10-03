package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.model.Session
import java.util.*

interface SessionService {

    fun create(): Session

    fun getById(sessionId: UUID): Session?

    fun delete(session: Session)

}