package com.lucasmdjl.passwordgenerator.server.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object Emails : UUIDTable() {

    val emailAddress = varchar("email_address", 64)
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(emailAddress, userId)
        index(isUnique = false, userId)
    }

}
