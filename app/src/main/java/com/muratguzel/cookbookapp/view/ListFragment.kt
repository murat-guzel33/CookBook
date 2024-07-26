package com.muratguzel.cookbookapp.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.muratguzel.cookbookapp.adapter.RecipeAdapter
import com.muratguzel.cookbookapp.databinding.FragmentListBinding
import com.muratguzel.cookbookapp.model.Recipe
import com.muratguzel.cookbookapp.roomdb.RecipeDAO
import com.muratguzel.cookbookapp.roomdb.RecipeDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: RecipeDatabase
    private lateinit var recipeDAO: RecipeDAO
    private val mDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(), RecipeDatabase::class.java, "Recipes")
            .build()
        recipeDAO = db.recipeDao()
    }

    private fun handleResponse(recipes: List<Recipe>) {
        Log.d("ListFragment", "handleResponse: ${recipes.size} tarif alındı.")

        val adapter = RecipeAdapter(recipes)
        binding.recyclerView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        getData()

        binding.floatingActionButton.setOnClickListener {
            val action =
                ListFragmentDirections.actionListFragmentToDetailsFragment(info = "new", id = 0)
            Navigation.findNavController(view).navigate(action)
        }
    }

    private fun getData() {
        Log.d("ListFragment", "getData: Tarifler veritabanından alınıyor.")

        mDisposable.add(
            recipeDAO.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { recipes ->
                        Log.d("ListFragment", "getData: Tarifler başarıyla alındı.")
                        handleResponse(recipes)
                    },
                    { error ->
                        Log.e(
                            "ListFragment",
                            "getData: Tarifler alınırken hata oluştu: ${error.localizedMessage}"
                        )
                    }
                )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}
