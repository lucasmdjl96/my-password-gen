package com.mypasswordgen.server.plugins


import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*


fun Application.installCompression() {
    pluginLogger.debug { "Installing Compression" }
    install(Compression) {
        gzip()
    }
}
