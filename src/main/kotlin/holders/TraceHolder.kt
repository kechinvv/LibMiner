package org.kechinvv.holders

import org.kechinvv.entities.MethodData
import org.kechinvv.utils.ExtractMethod

data class TraceHolder(val trace: List<MethodData>, val extractMethod: ExtractMethod, val count: Int) {
    fun getTargetClass(): String? {
        if (trace.isEmpty()) return null
        return trace.first().klass
    }
}
