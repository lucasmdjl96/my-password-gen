package com.lucasmdjl.application.model

import com.lucasmdjl.application.tables.Sites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Site(id: EntityID<Int>) : Entity<Int>(id) {

    var name by Sites.name
    var email by Email referencedOn Sites.email

    companion object : EntityClass<Int, Site>(Sites)

}