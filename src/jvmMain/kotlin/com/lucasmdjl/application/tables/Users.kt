package com.lucasmdjl.application.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Users : IntIdTable() {

    val username = varchar("username", 64)
    var session = reference("session_fk", Sessions.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(username, session)
    }

}