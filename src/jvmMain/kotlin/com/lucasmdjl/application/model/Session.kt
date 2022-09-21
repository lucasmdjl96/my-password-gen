package com.lucasmdjl.application.model

import com.lucasmdjl.application.tables.Sessions
import com.lucasmdjl.application.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Session(id: EntityID<UUID>) : Entity<UUID>(id) {

    val users by User referrersOn Users.session
    var password by Sessions.password

    companion object : EntityClass<UUID, Session>(Sessions)

}