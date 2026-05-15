package com.example.systemtoolkit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.systemtoolkit.databinding.ItemToolBinding
import com.example.systemtoolkit.model.Tool

class ToolAdapter(
    private val tools: List<Tool>,
    private val onItemClick: (Tool) -> Unit
) : RecyclerView.Adapter<ToolAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemToolBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tools[position])
    }

    override fun getItemCount(): Int = tools.size

    inner class ViewHolder(private val binding: ItemToolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: Tool) {
            binding.toolName.text = tool.name
            binding.toolDesc.text = tool.description
            binding.root.setOnClickListener { onItemClick(tool) }
        }
    }
}
