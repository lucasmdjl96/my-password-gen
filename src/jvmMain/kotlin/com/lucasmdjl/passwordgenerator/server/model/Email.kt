package com.lucasmdjl.passwordgenerator.server.model

import com.lucasmdjl.passwordgenerator.server.tables.Emails
import com.lucasmdjl.passwordgenerator.server.tables.Sites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Email(id: EntityID<Int>) : Entity<Int>(id) {

    var emailAddress by Emails.emailAddress
    var user by User referencedOn Emails.userId
    val sites by Site referrersOn Sites.emailId

    override fun toString(): String {
        return "[Email#${id.value}: $emailAddress]"
    }

    companion object : EntityClass<Int, Email>(Emails)

}