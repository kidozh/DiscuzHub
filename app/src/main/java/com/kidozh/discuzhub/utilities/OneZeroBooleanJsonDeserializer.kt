package com.kidozh.discuzhub.utilities

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

class OneZeroBooleanJsonDeserializer : JsonDeserializer<Boolean>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Boolean {
        val currentToken = p.currentToken
        when (currentToken) {

            JsonToken.VALUE_STRING -> {
                val text = p.text
                return !(text == "0" || text == "")
            }
            JsonToken.VALUE_TRUE -> {
                return true
            }
            JsonToken.VALUE_FALSE -> {
                return false
            }
            JsonToken.VALUE_NULL -> {
                return false
            }
            else -> return true
        }
    }

    override fun getNullValue(ctxt: DeserializationContext?): Boolean {
        return false
    }
}