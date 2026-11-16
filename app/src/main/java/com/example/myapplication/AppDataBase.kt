package com.example.myapplication.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.example.myapplication.Class_Jogador
import com.example.myapplication.Class_Partida
import com.example.myapplication.JogadorDAO
import com.example.myapplication.PartidaDAO

@Database(
    entities = [Class_Jogador::class, Class_Partida::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jogadorDAO(): JogadorDAO
    abstract fun partidaDAO(): PartidaDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder<AppDatabase>(
                    context.applicationContext,
                    "genius_database"
                ).setDriver(AndroidSQLiteDriver())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
