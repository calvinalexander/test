package com.clinicapp.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class FaceAnalysisResponse (
    @Json(name = "success")
    val status:Boolean,
    @Json(name = "id")
    val faceAnalysisID:Long
)