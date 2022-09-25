package com.lucasmdjl.application.model

import com.lucasmdjl.application.tables.Emails
import com.lucasmdjl.application.tables.Sites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Email(id: EntityID<Int>) : Entity<Int>(id) {

    var emailAddress by Emails.emailAddress
    var user by User referencedOn Emails.user
    val sites by Site referrersOn Sites.email

    companion object : EntityClass<Int, Email>(Emails)

}