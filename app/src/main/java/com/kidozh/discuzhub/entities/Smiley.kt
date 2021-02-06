package com.kidozh.discuzhub.entities

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.util.*
import kotlin.properties.Delegates


@Entity
data class Smiley(@JsonIgnore @PrimaryKey(autoGenerate = true) var id: Int) : Serializable {
    constructor(id: Int, code: String, image: String, i: Int) : this(id) {
        this.code = code;
        this.imageRelativePath = image
    }

    constructor(): this(0){

    }


    @JsonProperty("id")
    var smileyId: Int = 0

    lateinit var code: String
    @JsonProperty("image")
    lateinit var imageRelativePath: String


//    constructor(code: String, imageRelativePath: String) : this(id) {
//        this.code = code
//        this.imageRelativePath = imageRelativePath
//    }
//
//    constructor(code: String, image: String) {
//        this.code = code
//        this.imageRelativePath = imageRelativePath
//    }

    @JsonIgnore
    var discuzId : Int = 0
    @JsonIgnore
    var updateAt = Date()


    companion object{
        var DIFF_CALLBACK: DiffUtil.ItemCallback<Smiley> = object : DiffUtil.ItemCallback<Smiley>() {
            override fun areItemsTheSame(oldItem: Smiley, newItem: Smiley): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Smiley, newItem: Smiley): Boolean {
                return oldItem == newItem
            }
        }
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Smiley) return false

        if (id != other.id) return false
        if (updateAt != other.updateAt) return false
        if (code != other.code) return false
        if (imageRelativePath != other.imageRelativePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + updateAt.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + imageRelativePath.hashCode()
        return result
    }


}