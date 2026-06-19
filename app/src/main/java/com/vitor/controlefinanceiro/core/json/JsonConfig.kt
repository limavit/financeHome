package com.vitor.controlefinanceiro.core.json

import kotlinx.serialization.json.Json

object JsonConfig {
    val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
        encodeDefaults = true
    }
}
