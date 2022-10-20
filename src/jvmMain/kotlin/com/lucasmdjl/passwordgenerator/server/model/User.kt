package com.lucasmdjl.passwordgenerator.server.model

import com.lucasmdjl.passwordgenerator.server.tables.Emails
import com.lucasmdjl.passwordgenerator.server.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class User(id: EntityID<UUID>) : Entity<UUID>(id) {

    var username by Users.username
    val emails by Email referrersOn Emails.userId
    var session by Session referencedOn Users.sessionId
    var lastEmail by Email optionalReferencedOn Users.lastEmailId


    companion object : EntityClass<UUID, User>(Users)

}
