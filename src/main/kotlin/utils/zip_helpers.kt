package org.kechinvv.utils

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

fun decompressTarGz(tarGzFile: File, outputDir: File) {

    // Ensure canonical path for security
    val canonicalOutputDir = outputDir.canonicalFile

    if (!tarGzFile.exists()) throw FileNotFoundException("File not found: ${tarGzFile.path}")
    TarArchiveInputStream(
        GzipCompressorInputStream(
            BufferedInputStream(
                FileInputStream(tarGzFile)
            )
        )
    ).use { tarIn ->
        generateSequence { tarIn.nextEntry }.forEach { entry ->

            val outputFile = File(outputDir, entry.name).canonicalFile

            // Check if the extracted file stays inside outputDir
            // Prevent Zip Slip Vulnerability
            if (!outputFile.toPath().startsWith(canonicalOutputDir.toPath())) {
                throw SecurityException("Zip Slip vulnerability detected! Malicious entry: ${entry.name}")
            }

            if (entry.isDirectory) outputFile.mkdirs()
            else {
                outputFile.parentFile.mkdirs()
                outputFile.outputStream().use { outStream ->
                    tarIn.copyTo(outStream)
                }
            }
        }
    }

}