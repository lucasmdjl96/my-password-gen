package com.lucasmdjl.passwordgenerator.server.model

import com.lucasmdjl.passwordgenerator.server.tables.Sessions
import com.lucasmdjl.passwordgenerator.server.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Session(id: EntityID<UUID>) : Entity<UUID>(id) {

    val users by User referrersOn Users.sessionId

    override fun toString(): String {
        return "[Session#${id.value}]"
    }

    companion object : EntityClass<UUID, Session>(Sessions)

}