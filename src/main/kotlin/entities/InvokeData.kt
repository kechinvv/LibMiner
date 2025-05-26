package org.kechinvv.entities

import kotlinx.serialization.Serializable

@Serializable
data class InvokeData(val methodData: MethodData, val iHash: String, val date: String, val uuid: String)
