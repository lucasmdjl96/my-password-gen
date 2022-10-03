package com.lucasmdjl.passwordgenerator.server.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Emails : IntIdTable() {

    val emailAddress = varchar("email_address", 64)
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(emailAddress, userId)
    }

}