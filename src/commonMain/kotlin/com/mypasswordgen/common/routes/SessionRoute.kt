package com.mypasswordgen.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/session")
class SessionRoute
