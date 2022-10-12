package com.lucasmdjl.passwordgenerator.server.plugins


import io.ktor.server.application.*
import io.ktor.server.resources.*

fun Application.installResources() {
    pluginLogger.debug { "Installing Resources" }
    install(Resources)
}
