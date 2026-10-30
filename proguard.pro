-injars build/classes
-outjars build/preverified.jar

-libraryjars lib/cldc-1.1-stub.jar
-libraryjars lib/midp-2.0-stub.jar

-dontshrink
-dontoptimize
-dontobfuscate
-dontwarn
-ignorewarnings

# Emit CLDC-style StackMap attributes instead of Java SE StackMapTable.
-microedition

-keep class com.safaksayar.** { *; }
