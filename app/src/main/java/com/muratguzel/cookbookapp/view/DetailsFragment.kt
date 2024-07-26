package com.muratguzel.cookbookapp.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.muratguzel.cookbookapp.R
import com.muratguzel.cookbookapp.databinding.FragmentDetailBinding
import com.muratguzel.cookbookapp.model.Recipe
import com.muratguzel.cookbookapp.roomdb.RecipeDAO
import com.muratguzel.cookbookapp.roomdb.RecipeDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null
    private lateinit var db: RecipeDatabase
    private lateinit var recipeDAO: RecipeDAO
    private val mDisposable = CompositeDisposable()
    private var selectedRecipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("DetailsFragment", "onCreate: Başlatıldı.")

        registerLauncher()
        db = Room.databaseBuilder(requireContext(), RecipeDatabase::class.java, "Recipes")
            .build()
        recipeDAO = db.recipeDao()

        Log.d("DetailsFragment", "onCreate: Veritabanı ve DAO oluşturuldu.")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.d("DetailsFragment", "onCreateView: Görünüm oluşturuldu.")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("DetailsFragment", "onViewCreated: Görünüm görüntülendi.")

        binding.imageView.setOnClickListener { selectImage(it) }
        binding.saveButton.setOnClickListener { save(it) }
        binding.deleteButton.setOnClickListener { delete(it) }

        arguments?.let {
            val info = DetailsFragmentArgs.fromBundle(it).info
            if (info == "new") {
                selectedRecipe = null
                binding.deleteButton.isEnabled = false
                binding.saveButton.isEnabled = true
                binding.cookNameText.setText("")
                binding.materialText.setText("")
                binding.imageView.setImageResource(R.drawable.image)

                Log.d("DetailsFragment", "onViewCreated: Yeni tarif ekleme modu.")
            } else {
                binding.saveButton.isEnabled = false
                binding.deleteButton.isEnabled = true
                binding.imageView.isEnabled = false
                binding.cookNameText.isEnabled = false
                binding.materialText.isEnabled = false
                binding.cookNameText.setTextColor(Color.BLACK)
                binding.materialText.setTextColor(Color.BLACK)

                val id = DetailsFragmentArgs.fromBundle(it).id

                Log.d("DetailsFragment", "onViewCreated: Mevcut tarif yükleniyor, ID: $id")

                mDisposable.add(
                    recipeDAO.getById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            this::handleResponse
                        ) { error ->
                            Log.e(
                                "DetailsFragment",
                                "Tarif yüklenirken hata oluştu: ${error.localizedMessage}"
                            )
                        }
                )
            }
        }
    }

    private fun handleResponse(recipe: Recipe) {
        Log.d("DetailsFragment", "handleResponse: Tarif alındı, ad: ${recipe.name}")

        val bitmap = BitmapFactory.decodeByteArray(recipe.image, 0, recipe.image.size)
        binding.imageView.setImageBitmap(bitmap)
        binding.cookNameText.setText(recipe.name)
        selectedRecipe = recipe
        binding.materialText.setText(recipe.material)
    }

    fun save(view: View) {
        val name = binding.cookNameText.text.toString()
        val material = binding.materialText.text.toString()

        Log.d("DetailsFragment", "save: Tarif kaydediliyor, ad: $name")

        if (selectedBitmap != null) {
            Log.d("DetailsFragment", "save: Bitmap seçildi, kaydetme işlemi başlıyor.")

            val smallBitmap = createSmallBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()

            val recipe = Recipe(name, material, byteArray)

            // RxJava
            mDisposable.add(
                recipeDAO.insert(recipe)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Log.d("DetailsFragment", "Tarif başarıyla kaydedildi.")
                            handleResponseForInsert()
                        },
                        { error ->
                            Log.e(
                                "DetailsFragment",
                                "Tarif kaydedilirken hata oluştu: ${error.localizedMessage}"
                            )
                        }
                    )
            )
        } else {
            Log.w("DetailsFragment", "Bitmap seçilmedi, kaydetme işlemi iptal edildi.")
            Toast.makeText(requireContext(), "Lütfen bir resim seçin.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResponseForInsert() {
        Log.d(
            "DetailsFragment",
            "handleResponseForInsert: Tarif başarıyla eklendi, liste sayfasına yönlendiriliyor."
        )

        val action = DetailsFragmentDirections.actionDetailsFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun delete(view: View) {
        if (selectedRecipe != null) {
            Log.d("DetailsFragment", "delete: Tarif siliniyor, ad: ${selectedRecipe?.name}")

            mDisposable.add(
                recipeDAO.delete(recipe = selectedRecipe!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            Log.d("DetailsFragment", "Tarif başarıyla silindi.")
                            handleResponseForInsert()
                        },
                        { error ->
                            Log.e(
                                "DetailsFragment",
                                "Tarif silinirken hata oluştu: ${error.localizedMessage}"
                            )
                        }
                    )
            )
        }
    }

    fun selectImage(view: View) {
        Log.d("DetailsFragment", "selectImage: Resim seçme işlemi başlatıldı.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    //snackbar message
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission") {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }.show()

                    Log.d("DetailsFragment", "selectImage: Kullanıcıya izin gerekçesi gösterildi.")
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    Log.d("DetailsFragment", "selectImage: İzin talebi başlatıldı.")
                }
            } else {
                // open gallery
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

                Log.d("DetailsFragment", "selectImage: Galeri açılıyor.")
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //snackbar message
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission") {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }.show()

                    Log.d("DetailsFragment", "selectImage: Kullanıcıya izin gerekçesi gösterildi.")
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    Log.d("DetailsFragment", "selectImage: İzin talebi başlatıldı.")
                }
            } else {
                // open gallery
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

                Log.d("DetailsFragment", "selectImage: Galeri açılıyor.")
            }
        }
    }

    private fun registerLauncher() {
        Log.d("DetailsFragment", "registerLauncher: Activity sonuç başlatıcıları kayıt ediliyor.")

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //open gallery
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                    Log.d("DetailsFragment", "registerLauncher: İzin verildi, galeri açılıyor.")
                } else {
                    Toast.makeText(requireContext(), "Permission needed!", Toast.LENGTH_LONG).show()
                    Log.d("DetailsFragment", "registerLauncher: İzin verilmedi.")
                }
            }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectedImage = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver, intentFromResult.data!!
                                )
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver, selectedImage
                                )
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                            Log.d("DetailsFragment", "registerLauncher: Resim başarıyla seçildi.")
                        } catch (e: Exception) {
                            Log.e(
                                "DetailsFragment",
                                "registerLauncher: Resim seçilirken hata oluştu: ${e.localizedMessage}"
                            )
                        }
                    }
                } else {
                    Log.d("DetailsFragment", "registerLauncher: Resim seçme işlemi iptal edildi.")
                }
            }
    }

    private fun createSmallBitmap(UserSelectedBitmap: Bitmap, maximumSize: Int): Bitmap {
        Log.d("DetailsFragment", "createSmallBitmap: Küçük bitmap oluşturuluyor.")

        var width = UserSelectedBitmap.width
        var height = UserSelectedBitmap.height
        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            // Yatay
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            // Dikey
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        Log.d(
            "DetailsFragment",
            "createSmallBitmap: Küçük bitmap oluşturuldu, genişlik: $width, yükseklik: $height"
        )

        return Bitmap.createScaledBitmap(UserSelectedBitmap, width, height, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("DetailsFragment", "onDestroyView: Görünüm yok edildi.")

        _binding = null
        mDisposable.clear()

        Log.d("DetailsFragment", "onDestroyView: Disposable temizlendi.")
    }
}
