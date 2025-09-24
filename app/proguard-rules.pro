# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ------------- In-App Update Rules -------------

# Retrofit এবং এর সহযোগী OkHttp লাইব্রেরির জন্য নিয়ম
# এটি নিশ্চিত করে যে নেটওয়ার্কিং লাইব্রেরির কোনো অংশ মুছে না যায়
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okio.**
-keep class okio.** { *; }


# Kotlinx Serialization এবং আমাদের ডেটা ক্লাসের (UpdateInfo) জন্য নিয়ম
# এটি নিশ্চিত করে যে JSON পার্সিং এর জন্য ব্যবহৃত ডেটা ক্লাসের নাম পরিবর্তন না হয়
-keepclasseswithmembers public class * extends kotlinx.serialization.internal.GeneratedSerializer { *; }
-keep class *
-keep class com.contacts.vcf.utils.UpdateInfo { *; }
-keepclassmembers class com.contacts.vcf.utils.UpdateInfo { *; }


# ನಮ್ಮ Retrofit API সার্ভিস ইন্টারফেসের জন্য নিয়ম
# এটি নিশ্চিত করে যে Retrofit আমাদের বানানো ইন্টারফেসটি খুঁজে পায়
-keep interface com.contacts.vcf.utils.UpdateApiService { *; }

# OpenCSV এবং এর সহযোগী লাইব্রেরিগুলোর জন্য প্রয়োজনীয়
# কারণ এগুলো Android-এ নেই এমন Java ক্লাস ব্যবহার করার চেষ্টা করে
-dontwarn java.beans.**
-dontwarn javax.script.**
-dontwarn org.apache.commons.**

# -----------------------------------------------