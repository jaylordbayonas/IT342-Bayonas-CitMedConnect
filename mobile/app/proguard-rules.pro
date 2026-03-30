# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.google.gson.** { *; }
-keepclasseswithmembers class com.google.gson.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keepclasseswithmembers class okhttp3.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# App Models
-keep class edu.cit.bayonas.citmedconnect.data.model.** { *; }

# AppCompat
-keep public class androidx.appcompat.** { *; }
-keep class androidx.** { *; }
