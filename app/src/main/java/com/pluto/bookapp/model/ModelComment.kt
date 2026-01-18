package com.pluto.bookapp.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ModelComment(
    var id: String = "",
    var bookId: String = "",
    var timestamp: String = "",
    var comment: String = "",
    var uid: String = "",
    var name: String = "",
    var profileImage: String = ""
)
