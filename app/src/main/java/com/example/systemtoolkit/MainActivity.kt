package com.example.systemtoolkit

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.systemtoolkit.model.FeatureRegistry
import com.example.systemtoolkit.ui.ToolAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.tool_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = ToolAdapter(FeatureRegistry.tools) { tool ->
            startActivity(Intent(this, tool.targetActivity))
        }
        recyclerView.adapter = adapter
    }
}
