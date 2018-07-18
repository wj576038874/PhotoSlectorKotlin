package com.winfo.photoselector.entity

import java.io.Serializable

class Image(var path: String, var time: Long, var name: String) : Serializable {

    var position: Int = 0

    var isChecked: Boolean = false

    var selectPosition: Int = 0


}