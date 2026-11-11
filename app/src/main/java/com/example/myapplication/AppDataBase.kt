package com.example.myapplication.data

/*
import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase

//PREPARAÇÃO PARA A INSERÇÃO DA PERMANÊNCIA DOS DADOS NO BANCO DE DADOS!!!!!
@Database(
//    entities = [Class_Jogador::class, Class_Partida::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
//    abstract fun canalDao(): JogadorDAO
//    abstract fun videoDao(): PartidaDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genius_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
*/
