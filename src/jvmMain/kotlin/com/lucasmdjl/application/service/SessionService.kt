package com.lucasmdjl.application.service

import com.lucasmdjl.application.dto.SessionDto
import java.util.*

interface SessionService {

    fun create(): SessionDto

    fun getById(sessionId: UUID): SessionDto?

}