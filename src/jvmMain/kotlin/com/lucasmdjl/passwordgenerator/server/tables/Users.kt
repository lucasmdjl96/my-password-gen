package com.lucasmdjl.passwordgenerator.server.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Users : IntIdTable() {

    val username = varchar("username", 64)
    var sessionId = reference("session_id", Sessions.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(username, sessionId)
    }

}