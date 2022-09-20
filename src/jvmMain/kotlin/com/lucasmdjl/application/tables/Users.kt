package com.lucasmdjl.application.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {

    val username = varchar("username", 64)

}