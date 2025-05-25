package org.kechinvv.repository

import java.io.File

class MvnLocalRepository(file: File) : AbstractLocalRepository(file), Buildable {
    override fun build() {
        TODO("Not yet implemented")
    }

    override fun runTests() {
        TODO("Not yet implemented")
    }
}