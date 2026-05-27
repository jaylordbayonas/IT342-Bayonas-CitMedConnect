package edu.cit.bayonas.citmedconnect.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://127.0.0.1:8080/"

    // Set these after a successful login so all subsequent requests carry auth headers
    @Volatile var authToken: String? = null
    @Volatile var userId: String?    = null
    @Volatile var userRole: String?  = null

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder().apply {
            authToken?.let { addHeader("Authorization", "Bearer $it") }
            userId?.let    { addHeader("X-User-ID",     it) }
            userRole?.let  { addHeader("X-User-Role",   it.uppercase()) }
        }.build()
        chain.proceed(req)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val authService: AuthService               = retrofit.create(AuthService::class.java)
    val appointmentService: AppointmentService = retrofit.create(AppointmentService::class.java)
    val medicalRecordService: MedicalRecordService = retrofit.create(MedicalRecordService::class.java)
    val notificationService: NotificationService   = retrofit.create(NotificationService::class.java)
}
