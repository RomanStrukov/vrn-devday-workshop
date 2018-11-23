package com.tensorflow.humanrec.utility.permissions

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

object PermissionsHelper {
    private var callback: PermissionsCallback? = null

    fun requestPermissions(context: AppCompatActivity, permissions: Array<String>,
                           callback: PermissionsCallback) {
        this.callback = callback

        var isAllPermissionsGranted = false

        permissions.forEach {
            isAllPermissionsGranted = checkPermission(context, it)
            if (!isAllPermissionsGranted) {
                requestPermissions(context, permissions)
                return@forEach
            }
        }

        if (isAllPermissionsGranted) {
            this.callback?.granted()
        }
    }

    private fun requestPermissions(context: AppCompatActivity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(context,
                permissions,
                APP_PERMISSIONS)
    }

    private fun checkPermission(context: AppCompatActivity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            APP_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callback?.granted()
                } else {
                    callback?.disabled()
                }
            }
            else -> {
                // ignore
            }
        }
    }

    private const val APP_PERMISSIONS = 0
}
