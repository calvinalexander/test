package com.clinicapp.models

import com.squareup.moshi.JsonClass
@JsonClass(generateAdapter = true)
class FaceAnalysisRequest (
        val patient_id:Long
)