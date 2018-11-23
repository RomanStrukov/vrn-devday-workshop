package com.tensorflow.humanrec.utility.permissions

interface PermissionsCallback {
    fun granted()
    fun disabled()
}
