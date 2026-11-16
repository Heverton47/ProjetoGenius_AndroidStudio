package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.remote.FirestoreDesafioDataSource
import com.example.myapplication.data.repository.BancoRepository
import com.example.myapplication.data.repository.DesafioRepository

/**
 * Factory que injeta os Repositories nos ViewModels.
 * O Android não sabe criar um ViewModel que recebe parâmetros no construtor;
 * esta classe ensina como criar o GameViewModel e o AdminViewModel.
 */
class ViewModelFactory(
    private val bancoRepository: BancoRepository,
    private val desafioRepository: DesafioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        when {
            modelClass.isAssignableFrom(GameViewModel::class.java) ->
                GameViewModel(bancoRepository, desafioRepository, extras.createSavedStateHandle()) as T
            modelClass.isAssignableFrom(AdminViewModel::class.java) ->
                AdminViewModel(bancoRepository, desafioRepository) as T
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }

    companion object {
        /** Monta a Factory com o banco Room e o Firestore reais. */
        fun criar(context: Context): ViewModelFactory {
            val appContext = context.applicationContext
            return ViewModelFactory(
                BancoRepository(AppDatabase.getDatabase(appContext)),
                DesafioRepository(FirestoreDesafioDataSource())
            )
        }
    }
}
