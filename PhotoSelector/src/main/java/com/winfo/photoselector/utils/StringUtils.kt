package com.winfo.photoselector.utils

object StringUtils {

    fun isNotEmptyString(str: String?): Boolean {
        return str != null && str.isNotEmpty()
    }
}