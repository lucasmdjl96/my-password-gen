package com.lucasmdjl.application.model

import com.lucasmdjl.application.tables.Emails
import com.lucasmdjl.application.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Int>) : Entity<Int>(id) {

    var username by Users.username
    val emails by Email referrersOn Emails.user

    companion object : EntityClass<Int, User>(Users)

}