package com.lucasmdjl.application.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object Sessions : UUIDTable() {

    var password = varchar("password", 64).nullable()

}