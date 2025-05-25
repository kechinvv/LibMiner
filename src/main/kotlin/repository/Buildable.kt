package org.kechinvv.repository

import org.kechinvv.config.Configuration
import java.io.File

interface Buildable {
    fun build()

    fun runTests()

}