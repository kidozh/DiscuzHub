package com.kidozh.discuzhub.entities

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
class Smiley : Serializable {

    @PrimaryKey(autoGenerate = true)
    private val id = 0

    var updateAt = Date()

    lateinit var code: String
    lateinit var imageRelativePath: String
    var category: Int

    constructor(code: String, imageRelativePath: String, category: Int){
        this.code = code
        this.imageRelativePath = imageRelativePath
        this.category = category
    }

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
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + updateAt.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + imageRelativePath.hashCode()
        result = 31 * result + category
        return result
    }


}