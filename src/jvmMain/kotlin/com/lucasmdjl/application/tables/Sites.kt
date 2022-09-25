package com.lucasmdjl.application.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Sites : IntIdTable() {

    val name = varchar("name", 64)
    val email = reference("email_fk", Emails.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(name, email)
    }
}