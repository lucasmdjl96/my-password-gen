package com.lucasmdjl.application.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Emails : IntIdTable() {

    val emailAddress = varchar("email_address", 64).uniqueIndex()
    val user = reference("user_fk", Users.id, onDelete = ReferenceOption.CASCADE)

}