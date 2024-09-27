package com.notemaster

import com.google.gson.annotations.SerializedName

data class Urls(
    @SerializedName("small") val small: String,
    @SerializedName("regular") val regular: String,
    @SerializedName("full") val full: String
)