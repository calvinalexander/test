package com.clinicapp.models

data  class FaceCameraCaptureState (
    val position: CameraPositions,
    val isPreview: Boolean,
    val path: String? = null,
    val isNextValue: String? = "",
)