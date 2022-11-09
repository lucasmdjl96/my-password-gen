package com.mypasswordgen.server.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object Sessions : UUIDTable() {

    var lastUserId = reference("last_user_id", Users.id, onDelete = ReferenceOption.SET_NULL).nullable()

    init {
        index(isUnique = true, lastUserId)
    }

}
