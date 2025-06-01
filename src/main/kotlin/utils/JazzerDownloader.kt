package org.kechinvv.utils

import org.apache.commons.lang.SystemUtils
import org.kechinvv.UnsupportedOsForFuzzing
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*
import kotlin.jvm.optionals.getOrElse

class JazzerDownloader {

    fun getOrDownload(targetDir: Path = Paths.get("jazzer"), version: String = "0.24.0"): Path {
        targetDir.createDirectories()
        return Files.walk(targetDir)
            .filter { file -> !file.isDirectory() && file.nameWithoutExtension == "jazzer" }
            .findFirst()
            .getOrElse {
                val downloadLink =
                    URL("https://github.com/CodeIntelligenceTesting/jazzer/releases/download/v$version/jazzer-${this.getOs()}.tar.gz")
                downloadAndUnzip(targetDir, downloadLink)
            }

    }

    fun downloadAndUnzip(targetDir: Path, downloadLink: URL): Path {
        targetDir.createDirectories()
        val targetZip = targetDir.resolve("jazzer.tar.gz")
        downloadLink.openStream().use { if (targetZip.notExists()) Files.copy(it, targetZip) }
        decompressTarGz(targetZip.toFile(), targetDir.toFile())
        targetZip.deleteExisting()
        return Files.walk(targetDir).filter { !it.isDirectory() && it.nameWithoutExtension == "jazzer" }
            .findFirst()
            .get()
    }

    private fun getOs(): String {
        return if (SystemUtils.IS_OS_WINDOWS) {
            "windows"
        } else if (SystemUtils.IS_OS_LINUX) {
            "linux"
        } else if (SystemUtils.IS_OS_MAC) {
            "macos"
        } else throw UnsupportedOsForFuzzing()
    }

}

