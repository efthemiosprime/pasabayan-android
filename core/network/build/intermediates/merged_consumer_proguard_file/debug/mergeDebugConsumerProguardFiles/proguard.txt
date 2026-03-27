# Consumed by app when minify is enabled for Retrofit + OkHttp + kotlinx-serialization.
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
