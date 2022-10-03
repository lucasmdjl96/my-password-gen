package com.lucasmdjl.passwordgenerator.server.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Sites : IntIdTable() {

    val siteName = varchar("site_name", 64)
    val emailId = reference("email_id", Emails.id, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(siteName, emailId)
    }
}