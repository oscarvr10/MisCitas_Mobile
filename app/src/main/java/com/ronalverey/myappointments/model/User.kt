package com.ronalverey.myappointments.model

data class User (val id: Int, val name: String,
                 val email: String,
                 val idCard: String,
                 val address: String,
                 val phone: String,
                 val role: String){
    override fun toString(): String {
        return name
    }
}