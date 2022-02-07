package com.gajanan.messenger_chat.utils

import android.content.Context
import android.content.SharedPreferences
import com.gajanan.messenger_chat.utils.Constants.KEY_PREFERENCE_NAME

class PreferenceManager(context: Context) {

    private var sharePref: SharedPreferences? = null

    init {
        sharePref = context.getSharedPreferences(
            KEY_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun putBoolean(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = sharePref!!.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharePref!!.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        val editor: SharedPreferences.Editor = sharePref!!.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key:String) : String{
        return sharePref?.getString(key,null)!!
    }

    fun clear(){
      sharePref!!.edit().clear().apply()
    }
}