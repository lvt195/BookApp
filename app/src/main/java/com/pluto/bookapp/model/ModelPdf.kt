package com.pluto.bookapp.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ModelPdf(
    var uid: String = "",
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var categoryId: String = "",
    var url: String = "",
    var timestamp: Long = 0,
    var viewCount: Long = 0,
    var downloadCount: Long = 0
)
