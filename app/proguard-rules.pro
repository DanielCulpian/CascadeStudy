# Proguard / R8 keep rules for Cascade Study

# Room Persistence Library keep rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Room Entities and DAOs
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
