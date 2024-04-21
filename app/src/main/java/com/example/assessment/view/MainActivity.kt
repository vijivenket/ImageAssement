package com.example.assessment.view

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.LruCache
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assessment.MainRepository
import com.example.assessment.MainViewModel
import com.example.assessment.MyViewModelFactory
import com.example.assessment.databinding.ActivityMainBinding
import com.example.assessment.network.ApiClient

class MainActivity : AppCompatActivity() {
    // memory cache
    private lateinit var memoryCache: LruCache<String, Bitmap>
    lateinit var viewModel: MainViewModel

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapter = GridViewAdapter(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // setContentView(binding.root)
        val retrofitService = ApiClient.apiService
        val mainRepository = MainRepository(retrofitService)

        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(mainRepository)
        )[MainViewModel::class.java]

        binding.viewModel=viewModel


        binding.gridView.layoutManager=LinearLayoutManager(this)
        binding.gridView.adapter = GridViewAdapter(this)
//        adapter.updateList(arrayListOf())
//        viewModel.imageList.observe(this) {
//            adapter.updateList(it)
//        }
//        viewModel.errorMessage.observe(this) {
//            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
//        }
//        viewModel.getImageList()

    }
}