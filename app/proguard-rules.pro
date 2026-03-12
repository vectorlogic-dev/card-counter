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

# ---- Anti-crack hardening --------------------------------------------------
# Aggressively obfuscate everything not explicitly kept.
-repackageclasses ''
-allowaccessmodification
-flattenpackagehierarchy ''

# Obfuscate the security / monetization packages as much as possible.
# R8 will rename classes, methods, and fields; repackageclasses flattens
# the package structure so reversing the APK yields a flat namespace.
-keepclassmembers class com.vectorlogic.blackjackcounter.data.security.** {
    # Keep the public API method but obfuscate everything else.
    public static boolean isIntegrityOk(android.content.Context);
}

# Remove Android logging in release to avoid leaking debug info.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
