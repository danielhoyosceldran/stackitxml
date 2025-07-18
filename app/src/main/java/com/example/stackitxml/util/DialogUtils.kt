package com.example.stackitxml.util

import android.content.Context
import android.widget.Toast

/**
 * Classe d'utilitat per mostrar missatges a l'usuari.
 * Per a un projecte universitari, un Toast és suficient.
 * En una app real, aquí hi hauria diàlegs de progrés, alertes personalitzades, etc.
 */
object DialogUtils {

    /**
     * Mostra un missatge Toast de curta durada.
     * @param context El context de l'aplicació o activitat.
     * @param message El missatge a mostrar.
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Mostra un missatge Toast de llarga durada.
     * @param context El context de l'aplicació o activitat.
     * @param message El missatge a mostrar.
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
