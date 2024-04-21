package com.example.assessment.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assessment.MainRepository
import com.example.assessment.MainViewModel
import com.example.assessment.MyViewModelFactory
import com.example.assessment.R
import com.example.assessment.network.ApiClient

class GridviewActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gridview)
        val retrofitService = ApiClient.apiService
        val mainRepository = MainRepository(retrofitService)
        val adapter = GridViewAdapter(this)
        viewModel = ViewModelProvider(
            this,
            MyViewModelFactory(mainRepository)
        )[MainViewModel::class.java]

        val recyclerView=findViewById<RecyclerView>(R.id.recyclerView)
        val layoutManager = GridLayoutManager(this, 3)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter=adapter
        viewModel.imageList.observe(this) {
            adapter.updateList(it)
        }
        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.getImageList()
    }
}