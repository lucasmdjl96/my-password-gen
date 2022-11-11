package com.mypasswordgen.server.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDate
import org.jetbrains.exposed.sql.kotlin.datetime.date

object Sessions : UUIDTable() {

    var lastUserId = reference("last_user_id", Users.id, onDelete = ReferenceOption.SET_NULL).nullable()
    var dateCreated = date("date_created").defaultExpression(CurrentDate)

    init {
        index(isUnique = true, lastUserId)
    }

}
