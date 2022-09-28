package com.lucasmdjl.application.service

import com.lucasmdjl.application.model.Session
import java.util.*

interface SessionService {

    fun create(): Session

    fun getById(sessionId: UUID): Session?

    fun delete(session: Session)

}