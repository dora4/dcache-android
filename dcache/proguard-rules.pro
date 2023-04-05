# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/liuwenhao/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

# 保持注解不被混淆
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation {*;}
# 保持异常不被混淆
-keepattributes Exceptions
# 保持泛型不被混淆
-keepattributes Signature
# 保持内部类不被混淆
-keepattributes InnerClasses

-keep class dora.cache.** {
    *;
}
-dontwarn dora.cache.**

-keep class dora.db.builder.Condition
-keepclasseswithmembers public class dora.db.builder.WhereBuilder {
    public *;
}
-keepclasseswithmembers public class dora.db.builder.QueryBuilder {
    public *;
}
-keep class dora.db.constraint.AssignType
-keep class dora.db.converter.** {
    *;
}
-keep interface dora.db.dao.Dao {
    public *;
}
-keepclasseswithmembers public class dora.db.dao.OrmDao {
    public *;
}
-keepclasseswithmembers public class dora.db.dao.DaoFactory {
    public *;
}
-keep interface dora.db.table.OrmTable
-keep class dora.db.table.PrimaryKeyEntry
-keep class dora.db.table.PrimaryKeyId
-keepclasseswithmembers public class dora.db.table.TableManager {
    public *;
}
-keepclasseswithmembers public class dora.db.Orm {
    public *;
}
-keepclasseswithmembers public class dora.db.OrmConfig {
    public *;
}
-keepclasseswithmembers public class dora.db.OrmConfig$Builder {
    public *;
}
-keep public class dora.db.Transaction {
    public *;
}
-dontwarn dora.db.**

-keep public class dora.http.log.FormatLogInterceptor {
    public *;
}
-keep class dora.http.retrofit.** {
    *;
}
-keep class dora.http.DoraCallback {
    *;
}
-keep class dora.http.DoraListCallback {
    *;
}
-keepclasseswithmembers public class dora.http.DoraHttp {
    public *;
}
-dontwarn dora.http.**
