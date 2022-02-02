package com.kidozh.discuzhub.results

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kidozh.discuzhub.utilities.OneZeroBooleanJsonDeserializer

class PostParameterResult : BaseResult() {
    @JvmField
    @JsonProperty("Variables")
    var permissionVariables: PermissionVariablesResult = PermissionVariablesResult()

    class PermissionVariablesResult : VariableResults() {
        @JvmField
        @JsonProperty("allowperm")
        var allowPerm: AllowPermission = AllowPermission()
    }

    class AllowPermission {
        @JsonProperty("allowpost")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowPost = false

        @JsonProperty("allowreply")
        @JsonDeserialize(using = OneZeroBooleanJsonDeserializer::class)
        var allowReply = false

        @JvmField
        @JsonProperty("allowupload")
        var uploadSize: UploadSize = UploadSize()

        @JsonProperty("attachremain")
        var remainedAttachment: RemainedAttachment? = null

        @JvmField
        @JsonProperty("uploadhash")
        var uploadHash: String = ""
    }

    class UploadSize {
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var jpg = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var jpeg = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var gif = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var png = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var mp3 = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var txt = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var zip = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var rar = 0

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var pdf = 0
        val allowableFileSuffix: List<String>
            get() {
                val fileSuffixList: MutableList<String> = ArrayList()
                val fields = javaClass.declaredFields
                return try {
                    for (field in fields) {
                        field.isAccessible = true
                        val value = field.getInt(this)
                        if (value != 0) {
                            fileSuffixList.add(field.name)
                        }
                    }
                    fileSuffixList
                } catch (e: IllegalAccessException) {
                    fileSuffixList
                }
            }
    }

    class RemainedAttachment {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var size = 0

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        var count = 0
    }
}