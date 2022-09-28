package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.repository.SessionRepository
import java.util.*

object SessionRepositoryImpl : SessionRepository {

    override fun create(): Session =
        Session.new {}

    override fun getById(sessionId: UUID) =
        Session.findById(sessionId)

    override fun delete(session: Session) =
        session.delete()


}