package com.ronalverey.myappointments.io

import com.ronalverey.myappointments.model.Doctor
import com.ronalverey.myappointments.model.Specialty
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("specialties")
    abstract fun getSpecialties(): Call<ArrayList<Specialty>>

    @GET("specialties/{specialty}/doctors")
    abstract fun getDoctorsBySpecialty(@Path("specialty") specialtyId: Int): Call<ArrayList<Doctor>>

    companion object Factory {
        private const val BASE_URL = "https://test-appointments.azurewebsites.net/api/"
        //private const val BASE_URL = "http://my-appointments.test/api/"

        fun create() : ApiService {
            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}