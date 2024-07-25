package com.muratguzel.cookbookapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recipe(

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "material")
    var material: String,

    @ColumnInfo(name = "image")
    var image: ByteArray,

    ) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}