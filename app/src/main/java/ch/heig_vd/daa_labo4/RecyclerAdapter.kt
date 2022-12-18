package ch.heig_vd.daa_labo4

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URL
import java.time.Instant
import java.util.*

/*
 * Authors: Eliott Chytil, Maxim Golay & Lucien Perregaux
 */

class RecyclerAdapter(_coroutine_scope: LifecycleCoroutineScope, _cacheDir: File) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    val coroutine_scope = _coroutine_scope
    val cacheDir = _cacheDir
    var items = (1..10000).toList()

    suspend fun downloadImage(id: Int): ByteArray? = withContext(Dispatchers.IO) {
        try {
            URL("https://daa.iict.ch/images/${id}.jpg")
                .readBytes()
        }
        catch(e: IOException) {
            Log.w("", "Exception while downloading image", e)
            null
        }
    }

    suspend fun decodeImage(bytes: ByteArray): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size ?: 0)
        } catch (e: IOException) {
            Log.w("", "Exception while decoding image", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun readCache(id: Int): ByteArray? = withContext(Dispatchers.IO) {
        val file = File(cacheDir, "${id}.bmp")
        val seconds_elapsed = Instant.now().minusMillis(file.lastModified()).epochSecond

        if (file.exists() && seconds_elapsed < 300)
            return@withContext file.readBytes()
        else
            return@withContext null
    }

    suspend fun updateCache(id: Int, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val file = File(cacheDir, "${id}.bmp")
        file.writeBytes(bytes)
    }

    override fun getItemCount() = items.size

    // Not needed for this lab, we only have one type of view
    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_image, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image = view.findViewById<ImageView>(R.id.image)
        private val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)
        private var job: Job? = null

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(img_index: Int) {
            // Reset view status
            image.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

            // Stop previous job if still running
            job?.cancel()

            job = coroutine_scope.launch {
                var bytes = readCache(img_index)
                bytes = bytes ?: downloadImage(img_index)

                // if download failed return from coroutine
                if (bytes == null) return@launch

                updateCache(img_index, bytes)
                val bmp = decodeImage(bytes) ?: return@launch

                image.setImageBitmap(bmp)
                image.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }
    }
}

