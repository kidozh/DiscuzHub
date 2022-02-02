package com.kidozh.discuzhub.entities

import android.util.Log
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.entities.Poll
import java.io.IOException
import java.io.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
open class Poll : Serializable {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanDeserializer::class)
    var multiple = false

    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
    var expirations: Date? = null

    
    @JsonProperty("maxchoices")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var maxChoices = 0

    
    @JsonProperty("voterscount")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var votersCount = 0

    
    @JsonProperty("visiblepoll")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanDeserializer::class)
    var resultVisible = false

    
    @JsonProperty("allowvote")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonDeserialize(using = OneZeroBooleanDeserializer::class)
    var allowVote = false

    
    @JsonProperty("polloptions")
    @JsonDeserialize(using = OptionsDeserializer::class)
    var options: List<Option> = ArrayList()

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonIgnore
    var remaintime: List<String>? = null

    val checkedOptionNumber: Int
        get() = run {
            var count = 0
            for (i in options.indices) {
                if (options[i].checked) {
                    count += 1
                }
            }
            count
        }

    class Option : Serializable {
        
        @JsonProperty("polloptionid")
        var id: String = ""

        
        @JsonProperty("polloption")
        var name: String = ""

        
        @JsonProperty("votes")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var voteNumber = 0
        var width: String? = null

        
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var percent = 0f

        
        @JsonProperty("color")
        var colorName: String? = null

        
        @JsonProperty("imginfo")
        @JsonDeserialize(using = ImageInfoDeserializer::class)
        var imageInfo: ImageInfo? = null


        @JsonIgnore
        var checked = false
    }

    class ImageInfo : Serializable {
        var aid: String? = null
        var poid: String? = null
        var tid: String? = null
        var pid: String? = null
        var uid: String? = null
        var filename: String? = null
        var filesize: String? = null
        var attachment: String? = null
        var remote: String? = null
        var width: String? = null
        var thumb: String? = null

        @JsonProperty("dateline")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "s")
        var updateAt: Date? = null

        @JsonProperty("small")
        var smallURL: String? = null

        
        @JsonProperty("big")
        var bigURL: String? = null
    }

    class OptionsDeserializer : JsonDeserializer<List<Option>>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Option> {
            val currentToken = p.currentToken
            Log.d(TAG, "Get json text " + p.text)
            if (currentToken == JsonToken.START_OBJECT) {
                val codec = p.codec
                val optionMapperNode = codec.readTree<JsonNode>(p)
                var cnt = 1
                val objectMapper = ObjectMapper()
                val options: MutableList<Option> = ArrayList()
                while (true) {
                    val cntString = cnt.toString()
                    if (optionMapperNode.has(cntString)) {
                        val optionObj = optionMapperNode[cntString]
                        val parsedOption = objectMapper.treeToValue(optionObj, Option::class.java)
                        options.add(parsedOption)
                    } else {
                        break
                    }
                    cnt += 1
                }
                return options
            }
            return ArrayList()
        }
    }

    class ImageInfoDeserializer : JsonDeserializer<ImageInfo?>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ImageInfo? {
            val currentToken = p.currentToken
            if (currentToken == JsonToken.START_ARRAY) {
                return null
            } else if (currentToken == JsonToken.START_OBJECT) {
                val codec = p.codec
                return codec.readValue(p, ImageInfo::class.java)
            }
            return null
        }
    }

    class OneZeroBooleanDeserializer : JsonDeserializer<Boolean>() {
        @Throws(IOException::class)
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Boolean {
            val currentToken = jp.currentToken
            if (currentToken == JsonToken.VALUE_STRING) {
                val text = jp.text
                return if ("0" == text || "" == text) {
                    java.lang.Boolean.FALSE
                } else {
                    java.lang.Boolean.TRUE
                }
            } else if (currentToken == JsonToken.VALUE_NULL) {
                return java.lang.Boolean.FALSE
                //return null
            }
            throw ctxt.mappingException("Can't parse boolean value: " + jp.text)
        }
    }

    companion object {
        var TAG = Poll::class.java.simpleName
    }
}