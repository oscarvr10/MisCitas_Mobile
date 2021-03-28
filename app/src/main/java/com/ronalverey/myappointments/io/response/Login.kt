package com.ronalverey.myappointments.io.response

import com.ronalverey.myappointments.model.User

data class Login (val user: User, val jwt: String)