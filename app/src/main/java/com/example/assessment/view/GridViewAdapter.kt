package com.example.assessment.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.assessment.R
import com.example.assessment.disklrucache.DiskLruCache
import com.example.assessment.model.LoadImageItem
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class GridViewAdapter(context: Context) : RecyclerView.Adapter<GridViewAdapter.GridViewHolder>() {

    private var list = ArrayList<LoadImageItem>()
    private var memoryCache: LruCache<String, Bitmap>


    private val DISK_CACHE_SIZE = 1024 * 1024 * 10 // 10MB
    private val DISK_CACHE_SUBDIR = "thumbnails"
    private var diskLruCache: DiskLruCache? = null
    private val diskCacheLock = ReentrantLock()
    private val diskCacheLockCondition: Condition = diskCacheLock.newCondition()
    private var diskCacheStarting = true

    init {

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
        val cacheDir = getDiskCacheDir(context, DISK_CACHE_SUBDIR)
        InitDiskCacheTask().execute(cacheDir)
    }

    internal inner class InitDiskCacheTask : AsyncTask<File, Void, Void>() {
        override fun doInBackground(vararg params: File): Void? {
            diskCacheLock.withLock {
                val cacheDir = params[0]
                diskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE.toLong())
                diskCacheStarting = false // Finished initialization
                diskCacheLockCondition.signalAll() // Wake any waiting threads
            }
            return null
        }
    }

    fun loadBitmap(imageKey: String, urlString: String, imageView: ImageView) {

        getBitmapFromDiskCache(imageKey)?.also {
            imageView.setImageBitmap(it)
        } ?: run {
//            imageView.setImageResource(R.drawable.loader)
            val task = BitmapWorkerTask(imageView)
            task.execute(imageKey, urlString)
            null
        }
    }

    internal inner class BitmapWorkerTask(val imageView: ImageView) :
        AsyncTask<String, Unit, Bitmap>() {

        // Decode image in background.
        override fun doInBackground(vararg params: String): Bitmap? {
            val imageKey = params[0]
            val imageUrl = params[1]

            // Check disk cache in background thread
            return getBitmapFromDiskCache(imageKey) ?:
            // Not found in disk cache
            decodeBitmapFromStream(imageUrl, 200, 200)
                ?.also {
                    // Add final bitmap to caches
                    addBitmapToCache(imageKey, it)
                }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            imageView.setImageBitmap(result)
        }
    }

    private fun DownloadFromUrl(urlString: String): Bitmap? {
        return decodeBitmapFromStream(urlString, 100, 100)
    }

    private fun decodeBitmapFromStream(urlString: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        var url: URL? = null
        var `is`: InputStream? = null
        try {
            url = URL(urlString)
            `is` = url.content as InputStream
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(`is`, null, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // As InputStream can be used only once we have to regenerate it again.
        try {
            `is` = url!!.content as InputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeStream(`is`, null, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        inSampleSize = Math.min(width / reqWidth, height / reqHeight)
        return inSampleSize
    }

    fun addBitmapToCache(key: String, bitmap: Bitmap) {
        // Add to memory cache as before
        if (getBitmapFromDiskCache(key) == null) {
            memoryCache.put(key, bitmap)
        }

        // Also add to disk cache
        synchronized(diskCacheLock) {
            diskLruCache?.apply {
                if (!containsKey(key)) {
                    put(key, bitmap)
                }
            }
        }
    }

    fun getBitmapFromDiskCache(key: String): Bitmap? =
        diskCacheLock.withLock {
            // Wait while disk cache is started from background thread
            while (diskCacheStarting) {
                try {
                    diskCacheLockCondition.await()
                } catch (e: InterruptedException) {
                }

            }
            return diskLruCache?.getBitmap(key)
        }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
// but if not mounted, falls back on internal storage.
    fun getDiskCacheDir(context: Context, uniqueName: String): File {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        val cachePath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !isExternalStorageRemovable()
            ) {
                context.externalCacheDir?.path
            } else {
                context.cacheDir.path
            }

        return File(cachePath + File.separator + uniqueName)
    }


    fun updateList(imageList: List<LoadImageItem>?) {
        this.list = ArrayList()
        if (imageList != null) {
            this.list.addAll(imageList)
        }
        notifyDataSetChanged()

    }

    class GridViewHolder(itemView: View) : ViewHolder(itemView) {

        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        return GridViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {

        val loadImageItem = list[position]
        val imageUrl =
            loadImageItem.thumbnail.domain + "/" + loadImageItem.thumbnail.basePath + "/0/" + loadImageItem.thumbnail.key
        Log.d("Test", imageUrl)
//        holder.imageView.setImageBitmap(downloadBitmap(imageUrl))
        loadBitmap(loadImageItem.id,imageUrl,holder.imageView)
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


