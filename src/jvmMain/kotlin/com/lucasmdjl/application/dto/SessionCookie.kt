package com.lucasmdjl.application.dto

import io.ktor.server.auth.*

data class SessionCookie(val sessionId: String) : Principal