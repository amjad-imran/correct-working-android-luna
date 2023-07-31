package com.noisefit_commans.models

import java.io.File

class VisionOtaFiles(
    var url: String,
    var file: File,
    var fileName: String,
    var fileType: VisionOtaFileTypes
)

enum class VisionOtaFileTypes {
    Touch,
    Image,
    Heart,
    Firmware

}