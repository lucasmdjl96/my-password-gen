package com.lucasmdjl.application.dto

import io.ktor.server.auth.*
import java.util.*

data class SessionDto(val sessionId: UUID) : Principal
