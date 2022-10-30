package com.lucasmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.routes.AboutRoute
import com.lucasmdjl.passwordgenerator.common.routes.ContributeRoute
import io.ktor.server.application.*

interface AboutController {

    suspend fun get(call: ApplicationCall, aboutRoute: AboutRoute)

    suspend fun get(call: ApplicationCall, aboutRoute: ContributeRoute)

}
