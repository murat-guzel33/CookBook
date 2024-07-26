package com.muratguzel.cookbookapp.adapter

import android.app.DirectAction
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.muratguzel.cookbookapp.databinding.RecyclerRowBinding
import com.muratguzel.cookbookapp.model.Recipe
import com.muratguzel.cookbookapp.view.ListFragmentDirections

class RecipeAdapter(var recipeList: List<Recipe>) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
    class RecipeViewHolder(val binding: RecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val recyclerRowBinding: RecyclerRowBinding =
            RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = recipeList[position].name
        holder.itemView.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToDetailsFragment(
                info = "old",
                id = recipeList[position].id
            )
            Navigation.findNavController(it).navigate(action)
        }
    }
}