/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.service.downloads

import java.io.File

/** Resolves only the standard external-storage provider's primary-volume documents. */
internal object InternalStorageDocumentPath {
    fun resolve(
        authority: String?,
        documentId: String,
        storageRoot: File,
    ): File? {
        // DocumentsContract already decoded the ID. Decoding again would corrupt
        // literal percent escapes in filenames (for example, "photo%20one.jpg").
        val relativePath = documentId.removePrefix("primary:")
        val supportedDocument =
            authority == "com.android.externalstorage.documents" && documentId.startsWith("primary:")
        val relativeFilePath = relativePath.isNotEmpty() && !relativePath.startsWith('/') && '\\' !in relativePath
        if (!supportedDocument || !relativeFilePath) {
            return null
        }
        val root = storageRoot.canonicalFile
        val file = File(root, relativePath).canonicalFile
        return file.takeIf { it.path.startsWith(root.path + File.separator) }
    }
}
