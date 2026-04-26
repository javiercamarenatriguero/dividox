# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Keep Koin
-keep class org.koin.** { *; }
-keep interface org.koin.** { *; }

# Keep Firebase (when integrated)
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
