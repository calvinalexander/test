package com.clinicapp.models

import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
class CameraMeasurementRequest (
        val height:Int
)