package com.lucasmdjl.application

import kotlinx.html.*

fun HTML.index() {
    head {
        title("Password Generator")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/password-manager.js") {}
    }
}