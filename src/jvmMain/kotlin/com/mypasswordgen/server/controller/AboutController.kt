package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.ContributeRoute
import io.ktor.server.application.*

interface AboutController {

    suspend fun get(call: ApplicationCall, aboutRoute: AboutRoute)

    suspend fun get(call: ApplicationCall, aboutRoute: ContributeRoute)

}
