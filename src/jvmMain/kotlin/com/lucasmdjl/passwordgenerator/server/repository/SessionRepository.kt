package com.lucasmdjl.passwordgenerator.server.repository

import com.lucasmdjl.passwordgenerator.server.model.Session
import java.util.*

interface SessionRepository {

    fun create(): Session

    fun getById(sessionId: UUID): Session?

    fun delete(session: Session)

}