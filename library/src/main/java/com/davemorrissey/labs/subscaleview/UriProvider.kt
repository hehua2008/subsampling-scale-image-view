package com.davemorrissey.labs.subscaleview

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.concurrent.CancellationException

/**
 * @author hehua2008
 * @date 2022/4/2
 */
abstract class UriProvider {
    @WorkerThread
    @Throws(CancellationException::class, Exception::class)
    abstract fun provide(context: Context): Uri
}

class DefaultUriProvider : UriProvider {
    val uri: Uri

    constructor(uri: Uri) {
        var tmpUri = uri
        // #114 If file doesn't exist, attempt to url decode the URI and try again
        val uriString = tmpUri.toString()
        if (uriString.startsWith(ImageSource.FILE_SCHEME)) {
            val uriFile = File(uriString.substring(ImageSource.FILE_SCHEME.length - 1))
            if (!uriFile.exists()) {
                try {
                    tmpUri = Uri.parse(URLDecoder.decode(uriString, "UTF-8"))
                } catch (e: UnsupportedEncodingException) {
                    // Fallback to encoded URI. This exception is not expected.
                }
            }
        }
        this.uri = tmpUri
    }

    constructor(uri: String) {
        var tmpUri = uri
        if (!tmpUri.contains("://")) {
            if (tmpUri.startsWith("/")) {
                tmpUri = tmpUri.substring(1)
            }
            tmpUri = ImageSource.FILE_SCHEME + tmpUri
        }
        this.uri = Uri.parse(tmpUri)
    }

    @WorkerThread
    override fun provide(context: Context): Uri {
        return uri
    }
}

class ResourceUriProvider(val resId: Int) : UriProvider() {
    @Throws(Exception::class)
    override fun provide(context: Context): Uri {
        val scheme = ContentResolver.SCHEME_ANDROID_RESOURCE
        val pkgName = context.packageName
        return Uri.parse("$scheme://$pkgName/$resId")
    }
}
