package com.kidozh.discuzhub.entities

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.util.*
import kotlin.properties.Delegates

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
class Smiley(var code: String, var imageRelativePath: String, var category: Int) : Serializable {


    @PrimaryKey(autoGenerate = true)
    private val id = 0

    @JsonIgnore
    private var discuzId = 0

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