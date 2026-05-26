# ProGuard Rules para XZP Linked

# Keep Android classes
-keep class android.** { *; }
-keep class androidx.** { *; }

# Keep Kotlin classes
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep Model classes
-keep class com.xzplinked.app.model.** { *; }

# Keep Services
-keep class com.xzplinked.app.service.** { *; }

# Keep ViewModels
-keep class com.xzplinked.app.viewmodel.** { *; }

# Keep Fragments
-keep class com.xzplinked.app.ui.fragments.** { *; }

# Keep Adapters
-keep class com.xzplinked.app.ui.adapter.** { *; }

# Keep Receivers
-keep class com.xzplinked.app.receiver.** { *; }

# Keep Utilities
-keep class com.xzplinked.app.util.** { *; }

# Keep Retrofit
-keep class retrofit2.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod
-keepclasseswithmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Media3
-keep class androidx.media3.** { *; }

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Renaming
-allowaccessmodification
-repackageclasses
