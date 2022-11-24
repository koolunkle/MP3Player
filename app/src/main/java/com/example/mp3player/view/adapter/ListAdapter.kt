package com.example.mp3player.view.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3player.R
import com.example.mp3player.data.Music
import com.example.mp3player.database.DatabaseHelper
import com.example.mp3player.database.DatabaseHelper.Companion.DATABASE_NAME
import com.example.mp3player.database.DatabaseHelper.Companion.DATABASE_VERSION
import com.example.mp3player.databinding.AdapterListBinding
import com.example.mp3player.view.activity.MusicActivity
import java.text.SimpleDateFormat
import java.util.*

class ListAdapter(private val dataList: ArrayList<Music>) :
    RecyclerView.Adapter<ListAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(private val binding: AdapterListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Music) {
            binding.tvArtist.text = data.artist
            binding.tvTitle.text = data.title
            binding.tvDuration.text =
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(data.duration)

            val bitmap = data.getAlbumImage(itemView.context, ALBUM_SIZE)
            if (bitmap != null) binding.ivAlbum.setImageBitmap(bitmap)
            else binding.ivAlbum.setImageResource(R.drawable.music_note)

            when (data.likes) {
                LIKES_EMPTY -> binding.ivLikes.setImageResource(R.drawable.likes_empty)
                LIKES_FULL -> binding.ivLikes.setImageResource(R.drawable.likes_full)
            }

            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                binding.ivLikes.setOnClickListener {
                    when (data.likes) {
                        LIKES_EMPTY -> {
                            binding.ivLikes.setImageResource(R.drawable.likes_full)
                            data.likes = LIKES_FULL
                        }
                        LIKES_FULL -> {
                            binding.ivLikes.setImageResource(R.drawable.likes_empty)
                            data.likes = LIKES_EMPTY
                        }
                    }
                    val database =
                        DatabaseHelper(itemView.context, DATABASE_NAME, null, DATABASE_VERSION)
                    val flag = database.updateLikes(data)
                    if (!flag) Log.d("LisAdapter", "Failed to update likes")
                    else notifyItemChanged(position)
                }

                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, MusicActivity::class.java)
                    intent.putParcelableArrayListExtra("playlist", dataList)
                    intent.putExtra("position", position)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = AdapterListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    companion object {
        const val ALBUM_SIZE = 300
        const val LIKES_EMPTY = 0
        const val LIKES_FULL = 1
    }

}