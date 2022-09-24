package com.lucasmdjl.application.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*


fun Application.installCompression() {
    install(Compression) {
        gzip()
    }
}