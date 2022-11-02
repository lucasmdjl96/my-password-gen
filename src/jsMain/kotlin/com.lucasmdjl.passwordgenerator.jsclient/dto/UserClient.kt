package com.lucasmdjl.passwordgenerator.jsclient.dto

import kotlinx.serialization.Serializable

@Serializable
class UserClient(val username: String, val emailList: MutableList<String>) {

    constructor(username: String) : this(username, mutableListOf())

    fun hasEmail(emailAddress: String): Boolean = emailList.find { it == emailAddress } != null

    fun addEmail(emailAddress: String) = emailList.add(emailAddress)

    fun removeEmail(emailAddress: String) = emailList.remove(emailAddress)

    override fun toString(): String {
        return "[UserDto: $username]"
    }

}
