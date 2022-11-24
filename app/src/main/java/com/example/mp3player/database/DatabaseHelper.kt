package com.example.mp3player.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.mp3player.data.Music

class DatabaseHelper(
    context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(database: SQLiteDatabase?) {
        val query = """
            create table $TABLE_NAME(id text primary key, title text, artist text, albumId text, duration integer, likes integer)
        """.trimIndent()
        database?.execSQL(query)
    }

    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val query = """drop table $TABLE_NAME""".trimIndent()
        database?.execSQL(query)
        onCreate(database)
    }

    fun selectAllRecords(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf()
        var cursor: Cursor? = null
        val query = """select * from $TABLE_NAME""".trimIndent()
        val database = this.readableDatabase
        try {
            cursor = database.rawQuery(query, null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            } else musicList = null

        } catch (e: Exception) {
            Log.d("DatabaseHelper", "Failed to select all records : ${e.printStackTrace()}")
        }
        cursor?.close()
        return musicList
    }

    fun selectLikes(): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf()
        var cursor: Cursor? = null
        val query = """select * from $TABLE_NAME where likes = 1""".trimIndent()
        val database = this.readableDatabase
        try {
            cursor = database.rawQuery(query, null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            } else musicList = null

        } catch (e: Exception) {
            Log.d("DatabaseHelper", "Failed to select likes : ${e.printStackTrace()}")
        }
        cursor?.close()
        return musicList
    }

    fun selectTitle(queries: String): MutableList<Music>? {
        var musicList: MutableList<Music>? = mutableListOf()
        var cursor: Cursor? = null
        val query =
            """select * from $TABLE_NAME where title like '${queries}%' or artist like '${queries}%'""".trimIndent()
        val database = this.readableDatabase
        try {
            cursor = database.rawQuery(query, null)
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val title = cursor.getString(1)
                    val artist = cursor.getString(2)
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val likes = cursor.getInt(5)
                    val music = Music(id, title, artist, albumId, duration, likes)
                    musicList?.add(music)
                }
            } else musicList = null

        } catch (e: Exception) {
            Log.d("DatabaseHelper", "Failed to select title : ${e.printStackTrace()}")
        }
        cursor?.close()
        return musicList
    }

    fun insertAllRecords(music: Music): Boolean {
        val flag: Boolean
        val query =
            """insert into $TABLE_NAME(id, title, artist, albumId, duration, likes) values('${music.id}', '${music.title}', '${music.artist}', '${music.albumId}', '${music.duration}', '${music.likes}')""".trimIndent()
        val database = this.writableDatabase
        flag = try {
            database.execSQL(query)
            true
        } catch (e: Exception) {
            Log.d("DatabaseHelper", "Failed to insert all records : ${e.printStackTrace()}")
            false
        }
        return flag
    }

    fun updateLikes(music: Music): Boolean {
        val flag: Boolean
        val query =
            """update $TABLE_NAME set likes = ${music.likes} where id = '${music.id}'""".trimIndent()
        val database = this.writableDatabase
        flag = try {
            database.execSQL(query)
            true
        } catch (e: Exception) {
            Log.d("DatabaseHelper", "Failed to update likes : ${e.printStackTrace()}")
            false
        }
        return flag
    }

    companion object {
        const val DATABASE_NAME = "musicDB"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "musicTBL"
    }

}