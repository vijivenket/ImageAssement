package com.example.assessment.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.assessment.R
import java.net.URL

class ImgeViewAdapter: RecyclerView.Adapter<ImgeViewAdapter.GridViewHolder>() {

    private val list= ArrayList<String>()

    fun updateList(list: List<String>?){
        this.list.clear()
        if (list != null) {
            this.list.addAll(list)
        }
        notifyDataSetChanged()

    }

    class GridViewHolder(itemView: View) : ViewHolder(itemView) {

        val imageView=itemView.findViewById<ImageView>(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        return GridViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view,parent,false))
    }

    override fun getItemCount()=10

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {

//        val loadImageItem=list[position]
//
//        val imageUrl=loadImageItem.thumbnail.domain+"/"+loadImageItem.thumbnail.basePath+"/0/"+loadImageItem.thumbnail.key
        Log.d("Test","imageUrl")
        holder.imageView.setImageResource(R.drawable.ic_launcher_background)
    }

    private fun downloadBitmap(imageUrl: String): Bitmap? {
        return try {
            val conn = URL(imageUrl).openConnection()
            conn.connect()
            val inputStream = conn.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        } catch (e: Exception) {
            Log.e("testimg", "Exception $e")
            null
        }
    }

}


