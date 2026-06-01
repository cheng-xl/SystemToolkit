package com.example.systemtoolkit.feature.wechat

import android.content.Context
import android.content.SharedPreferences

object GamePackages {

    private const val PREFS_NAME = "game_packages"
    private const val KEY_PACKAGES = "package_set"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PACKAGES, emptySet()) ?: emptySet()

    fun add(context: Context, pkg: String) {
        val set = getAll(context).toMutableSet()
        set.add(pkg.trim())
        prefs(context).edit().putStringSet(KEY_PACKAGES, set).apply()
    }

    fun remove(context: Context, pkg: String) {
        val set = getAll(context).toMutableSet()
        set.remove(pkg.trim())
        prefs(context).edit().putStringSet(KEY_PACKAGES, set).apply()
    }

    fun contains(context: Context, pkg: String): Boolean =
        pkg in getAll(context)
}
