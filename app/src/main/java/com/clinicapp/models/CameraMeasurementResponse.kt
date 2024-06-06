package com.clinicapp.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CameraMeasurementResponse (
    @Json(name = "success")
    val status:Boolean,
    @Json(name = "data")
    val data:Data

)
@JsonClass(generateAdapter = true)
class Data(
        val patient_id:Long,
        val height:Long,
        val created_at:String,
        val updated_at:String
)