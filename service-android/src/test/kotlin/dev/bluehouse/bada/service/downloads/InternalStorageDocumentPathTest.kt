/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.service.downloads

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class InternalStorageDocumentPathTest {
    @TempDir
    lateinit var root: File

    @Test
    fun `resolves nested Pictures file without decoding its name again`() {
        val file = resolve("primary:Pictures/Shared/photo %20 한글.jpg")

        assertThat(file).isEqualTo(File(root, "Pictures/Shared/photo %20 한글.jpg").canonicalFile)
    }

    @Test
    fun `rejects other providers and storage volumes`() {
        assertThat(InternalStorageDocumentPath.resolve("cloud.documents", "primary:Pictures/photo.jpg", root)).isNull()
        assertThat(InternalStorageDocumentPath.resolve(null, "primary:Pictures/photo.jpg", root)).isNull()
        assertThat(resolve("1234-5678:Pictures/photo.jpg")).isNull()
        assertThat(resolve("Pictures/photo.jpg")).isNull()
    }

    @Test
    fun `rejects paths outside storage root and root itself`() {
        listOf(
            "primary:",
            "primary:.",
            "primary:/Pictures/photo.jpg",
            "primary:../photo.jpg",
            "primary:Pictures/../../photo.jpg",
            "primary:../${root.name}-sibling/photo.jpg",
            "primary:Pictures\\photo.jpg",
        ).forEach { documentId ->
            assertThat(resolve(documentId)).isNull()
        }
    }

    private fun resolve(documentId: String): File? =
        InternalStorageDocumentPath.resolve("com.android.externalstorage.documents", documentId, root)
}
