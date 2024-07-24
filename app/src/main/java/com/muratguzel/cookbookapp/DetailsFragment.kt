package com.muratguzel.cookbookapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muratguzel.cookbookapp.databinding.FragmentDetailBinding
import com.muratguzel.cookbookapp.databinding.FragmentListBinding

class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener { selectImage(it) }
        binding.saveButton.setOnClickListener { save(it) }
        binding.deleteButton.setOnClickListener { delete(it) }
        arguments?.let {
            val info = DetailsFragmentArgs.fromBundle(it).info
            if (info == "new") {
                // new recipe will be added
                binding.deleteButton.isEnabled = false
                binding.saveButton.isEnabled = true
                binding.cookNameText.setText("")
                binding.materialText.setText("")
                binding.imageView.setImageResource(R.drawable.ic_launcher_background)
            } else {
                //The old added tariff will be checked.
                binding.saveButton.isEnabled = false
                binding.deleteButton.isEnabled = true
                binding.imageView.isEnabled = false
                binding.cookNameText.isEnabled = false
                binding.materialText.isEnabled = false
            }
        }
    }

    fun save(view: View) {

    }

    fun delete(view: View) {

    }

    fun selectImage(view: View) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

