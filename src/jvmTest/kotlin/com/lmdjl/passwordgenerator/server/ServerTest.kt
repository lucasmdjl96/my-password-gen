package com.lmdjl.passwordgenerator.server

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

fun HttpRequestBuilder.configurePath() {
    url {
        protocol = URLProtocol.HTTPS
        host = "localhost"
        port = 443
    }
}

suspend inline fun HttpClient.gets(url: String, block: HttpRequestBuilder.() -> Unit = {}) =
    get(url) {
        configurePath()
        block()
    }

suspend inline fun HttpClient.posts(url: String, block: HttpRequestBuilder.() -> Unit = {}) =
    post(url) {
        configurePath()
        block()
    }

suspend inline fun HttpClient.deletes(url: String, block: HttpRequestBuilder.() -> Unit = {}) =
    delete(url) {
        configurePath()
        block()
    }

suspend inline fun HttpClient.puts(url: String, block: HttpRequestBuilder.() -> Unit = {}) =
    put(url) {
        configurePath()
        block()
    }

class ServerTest
