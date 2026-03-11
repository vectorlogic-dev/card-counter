# Keep line numbers and source file names in release stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Kotlin ----------------------------------------------------------------
# Kotlin Metadata is read by coroutines and serialization via reflection.
-keep class kotlin.Metadata { *; }
# Coroutines use volatile fields for internal locking; preserve them under R8.
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.** {
    volatile <fields>;
}

# ---- Jetpack Lifecycle / ViewModel -----------------------------------------
# Anonymous ViewModelProvider.Factory subclasses are only referenced at runtime.
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.lifecycle.ViewModelProvider$Factory
