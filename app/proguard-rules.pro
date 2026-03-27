# Pasabayan — expand when release minification is enabled.

# Keep line numbers for Play Console / crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Retrofit / OkHttp (when minify is on) ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# --- Kotlin serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    *** INSTANCE;
}

# --- Hilt ---
-dontwarn dagger.internal.codegen.**

# --- Firebase / FCM ---
-keepattributes Signature
-keepclassmembers class * {
    @com.google.firebase.messaging.FirebaseMessagingService <methods>;
}
