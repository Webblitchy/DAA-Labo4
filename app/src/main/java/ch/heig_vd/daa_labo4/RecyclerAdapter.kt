package ch.heig_vd.daa_labo4

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

/*
 * Authors: Eliott Chytil, Maxim Golay & Lucien Perregaux
 */

class RecyclerAdapter(_coroutine_scope: LifecycleCoroutineScope) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    val coroutine_scope = _coroutine_scope
    var items = listOf<Int>()

    set(value) {
        val diffCallback = DiffCallback(items, value)
        val diffItems = DiffUtil.calculateDiff(diffCallback)
        field = value
        diffItems.dispatchUpdatesTo(this)
    }

    init {
        items = (1..12).toList()
    }

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

    suspend fun decodeImage(bytes: ByteArray?): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes?.size ?: 0)
        } catch (e: IOException) {
            Log.w("", "Exception while decoding image", e)
            null
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image = view.findViewById<ImageView>(R.id.image)

        fun bind(img_index: Int) {
            // TODO: handle cache
            coroutine_scope.launch {
                val bytes = downloadImage(img_index)
                val bmp = decodeImage(bytes)

                // TODO: display image
                image.setImageBitmap(bmp)
                image.visibility = View.VISIBLE
            }
        }
    }
}

// TODO: faire
class DiffCallback(private val oldList: List<Int>, private val newList: List<Int>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return false
    }
    override fun areContentsTheSame(oldItemPosition : Int, newItemPosition : Int): Boolean {
        return false
    }
}
