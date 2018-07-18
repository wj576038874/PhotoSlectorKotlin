package com.winfo.photoselector.entity

import com.winfo.photoselector.utils.StringUtils
import java.io.Serializable
import java.util.ArrayList

class Folder : Serializable {

    var useCamera: Boolean = false
    var name: String? = null
    var images: ArrayList<Image>? = null

    fun addImage(image: Image?) {
        if (image != null && StringUtils.isNotEmptyString(image.path)) {
            if (images == null) {
                images = ArrayList()
            }
            images!!.add(image)
        }
    }
}