package cl.duocuc.miregistro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.duocuc.miregistro.data.SesionPreferencias
import cl.duocuc.miregistro.data.UsuarioDbHelper

class LoginViewModelFactory(
    private val dbHelper: UsuarioDbHelper,
    private val sesionPrefs: SesionPreferencias
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(dbHelper, sesionPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}