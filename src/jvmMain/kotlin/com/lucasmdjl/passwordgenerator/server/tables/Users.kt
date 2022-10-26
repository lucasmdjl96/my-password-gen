package com.lucasmdjl.passwordgenerator.server.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object Users : UUIDTable() {

    val username = varchar("username", 64)
    var sessionId = reference("session_id", Sessions.id, onDelete = ReferenceOption.CASCADE)
    var lastEmailId = reference("last_email_id", Emails.id, onDelete = ReferenceOption.SET_NULL).nullable()

    init {
        uniqueIndex(username, sessionId)
        index(isUnique = false, sessionId)
        index(isUnique = false, lastEmailId)
    }

}
