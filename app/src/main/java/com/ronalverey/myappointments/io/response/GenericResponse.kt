package com.ronalverey.myappointments.io.response

data class GenericResponse<T>(val success: Boolean, val data: T)
