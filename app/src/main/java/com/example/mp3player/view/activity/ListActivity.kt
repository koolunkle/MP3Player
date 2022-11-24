package com.example.mp3player.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.mp3player.R
import com.example.mp3player.data.Music
import com.example.mp3player.database.DatabaseHelper
import com.example.mp3player.database.DatabaseHelper.Companion.DATABASE_NAME
import com.example.mp3player.database.DatabaseHelper.Companion.DATABASE_VERSION
import com.example.mp3player.databinding.ActivityListBinding
import com.example.mp3player.view.adapter.ListAdapter

class ListActivity : AppCompatActivity() {

    private var _binding: ActivityListBinding? = null

    private val binding get() = _binding!!

    private var backPressedTime: Long = 0

    private var musicList: MutableList<Music>? = mutableListOf()

    private lateinit var listAdapter: ListAdapter

    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    private val database: DatabaseHelper by lazy {
        DatabaseHelper(this, DATABASE_NAME, null, DATABASE_VERSION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_list)
        binding.apply {
            startMusicPlayer()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_likes -> {
                musicList?.clear()
                database.selectLikes()?.let { musicList?.addAll(it) }
                listAdapter.notifyDataSetChanged()
            }
            R.id.menu_list -> {
                musicList?.clear()
                database.selectAllRecords()?.let { musicList?.addAll(it) }
                listAdapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_music, menu)
        val searchMenu = menu?.findItem(R.id.menu_search)
        val searchView = searchMenu?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query.isNullOrBlank()) {
                    musicList?.clear()
                    database.selectAllRecords()?.let { musicList?.addAll(it) }
                    listAdapter.notifyDataSetChanged()
                } else {
                    musicList?.clear()
                    database.selectTitle(query)?.let { musicList?.addAll(it) }
                    listAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun startMusicPlayer() {
        if (isPermitted()) startMusicProcess()
        else ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION)
    }

    private fun startMusicProcess() {
        musicList = database.selectAllRecords()
        if (musicList.isNullOrEmpty()) {
            val tempMusicList = getMusicList()
            if (tempMusicList?.isNotEmpty() == true) {
                for (i in 0 until tempMusicList.size) {
                    val music = tempMusicList[i]
                    database.insertAllRecords(music)
                }
                musicList = tempMusicList
            } else Toast.makeText(this, "The MP3 file does not exist", Toast.LENGTH_SHORT).show()

        }
        listAdapter = ListAdapter(musicList as ArrayList<Music>)
        binding.recyclerView.apply {
            adapter = listAdapter
            setHasFixedSize(true)
        }
    }

    private fun getMusicList(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf()
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        val cursor = contentResolver.query(musicUri, projection, null, null, null)
        if (cursor?.count!! > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val title = cursor.getString(1).replace("'", "")
                val artist = cursor.getString(2).replace("'", "")
                val albumId = cursor.getString(3)
                val duration = cursor.getInt(4)
                val music = Music(id, title, artist, albumId, duration, LIKES_EMPTY)
                musicList?.add(music)
            }
        } else musicList = null

        cursor.close()
        return musicList
    }

    private fun isPermitted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, permissions[0]
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMusicProcess()
        } else {
            Toast.makeText(this, "Please approve the permissions", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime >= 1500) {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(
                this, resources.getString(R.string.toast_back_pressed), Toast.LENGTH_SHORT
            ).show()
        } else ActivityCompat.finishAffinity(this)

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val REQUEST_PERMISSION = 1004
        const val LIKES_EMPTY = 0
    }

}