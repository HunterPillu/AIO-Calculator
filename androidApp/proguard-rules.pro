# Preserve source locations in release stack traces while obfuscating class
# and method names. Store the generated mapping file securely.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kermit uses platform log writers and remains part of release diagnostics.
-keep class co.touchlab.kermit.** { *; }
