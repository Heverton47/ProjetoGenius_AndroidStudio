package com.example.myapplication.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.driver.AndroidSQLiteDriver

@Database(
    entities = [Class_Jogador::class, Class_Partida::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jogadorDAO(): JogadorDAO
    abstract fun partidaDAO(): PartidaDAO

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE jogadores ADD COLUMN recorde INTEGER NOT NULL DEFAULT 0")
                connection.execSQL(
                    "UPDATE jogadores SET recorde = COALESCE(" +
                        "(SELECT MAX(pontuacao) FROM partidas WHERE jogadorId = jogadores.id), 0)"
                )
                connection.execSQL("ALTER TABLE partidas ADD COLUMN sessaoId TEXT")
                connection.execSQL("CREATE UNIQUE INDEX index_partidas_sessaoId ON partidas(sessaoId)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder<AppDatabase>(
                    context.applicationContext,
                    "genius_database"
                ).setDriver(AndroidSQLiteDriver())
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
