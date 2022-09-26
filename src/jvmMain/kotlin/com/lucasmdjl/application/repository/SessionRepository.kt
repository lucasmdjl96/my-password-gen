package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Session
import java.util.*

interface SessionRepository {

    fun create(): Session

    fun getById(sessionId: UUID): Session?

    fun delete(session: Session)

}