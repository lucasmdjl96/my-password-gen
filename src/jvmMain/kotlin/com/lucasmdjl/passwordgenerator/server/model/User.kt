package com.lucasmdjl.passwordgenerator.server.model

import com.lucasmdjl.passwordgenerator.server.tables.Emails
import com.lucasmdjl.passwordgenerator.server.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Int>) : Entity<Int>(id) {

    var username by Users.username
    val emails by Email referrersOn Emails.userId
    var session by Session referencedOn Users.sessionId

    override fun toString(): String {
        return "[User#${id.value}: $username]"
    }

    companion object : EntityClass<Int, User>(Users)

}