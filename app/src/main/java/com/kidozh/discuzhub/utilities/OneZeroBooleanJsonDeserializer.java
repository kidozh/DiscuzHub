package com.kidozh.discuzhub.utilities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class OneZeroBooleanJsonDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken currentToken = p.getCurrentToken();
        if (currentToken.equals(JsonToken.VALUE_STRING)) {
            String text = p.getText();
            if (text.equals("0") || text.equals("")) {
                return Boolean.FALSE;
            } else {
                return Boolean.TRUE;
            }

        } else if (currentToken.equals(JsonToken.VALUE_TRUE)) {
            return Boolean.TRUE;
        } else if (currentToken.equals(JsonToken.VALUE_FALSE)) {
            return Boolean.FALSE;
        } else if (currentToken.equals(JsonToken.VALUE_NULL)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
