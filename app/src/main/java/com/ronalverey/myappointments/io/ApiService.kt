package com.ronalverey.myappointments.io

import com.ronalverey.myappointments.io.response.BooleanResponse
import com.ronalverey.myappointments.io.response.GenericResponse
import com.ronalverey.myappointments.io.response.Login
import com.ronalverey.myappointments.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @GET("specialties")
    fun getSpecialties(): Call<GenericResponse<ArrayList<Specialty>>>

    @GET("user")
    @Headers("Accept: application/json")
    fun getUser(@Header("Authorization") authHeader: String): Call<GenericResponse<User>>

    @POST("user")
    @Headers("Accept: application/json")
    fun postUser(
        @Header("Authorization") authHeader: String,
        @Query("name") name: String,
        @Query("phone") phone: String,
        @Query("address") address: String
    ): Call<Void>

    @GET("specialties/{specialty}/doctors")
    fun getDoctorsBySpecialty(@Path("specialty") specialtyId: Int): Call<GenericResponse<ArrayList<Doctor>>>

    @GET("schedule/hours")
    fun getAvailableHours(@Query("doctor_id") doctorId: Int, @Query("date") date: String): Call<GenericResponse<Schedule>>

    @POST("login")
    fun postLogin(@Query("email") email: String, @Query("password") password: String): Call<GenericResponse<Login>>

    @POST("logout")
    fun postLogout(@Header("Authorization") authHeader: String): Call<Void>

    @GET("appointments")
    fun getAppointments(@Header("Authorization") authHeader: String): Call<GenericResponse<ArrayList<Appointment>>>

    @POST("appointments")
    @Headers("Accept: application/json")
    fun postAppointment(
        @Header("Authorization") authHeader: String,
        @Query("description") description: String,
        @Query("specialty_id") specialtyId: Int,
        @Query("doctor_id") doctorId: Int,
        @Query("scheduled_date") scheduledDate: String,
        @Query("scheduled_time") scheduledTime: String,
        @Query("type") type: String
    ): Call<BooleanResponse>

    @POST("fcm/token")
    fun postToken(
        @Header("Authorization") authHeader: String,
        @Query("device_token") token: String?
    ): Call<Void>

    @POST("register")
    @Headers("Accept: application/json")
    fun postRegister(
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("password_confirmation") pwdConfirmation: String
    ): Call<GenericResponse<Login>>

    companion object Factory {
        private const val BASE_URL = "https://my-appointments.azurewebsites.net/api/"
        //private const val BASE_URL = "http://my-appointments.test/api/"

        fun create() : ApiService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}