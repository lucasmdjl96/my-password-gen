package com.lucasmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.routes.AboutRoute
import io.ktor.server.application.*

interface AboutController {

    suspend fun get(call: ApplicationCall, aboutRoute: AboutRoute)

}
