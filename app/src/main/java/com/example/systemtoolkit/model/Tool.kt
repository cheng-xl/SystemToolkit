package com.example.systemtoolkit.model

import android.app.Activity

data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val targetActivity: Class<out Activity>
)
