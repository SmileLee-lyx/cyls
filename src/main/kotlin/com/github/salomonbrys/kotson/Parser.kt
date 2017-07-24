package com.github.salomonbrys.kotson

import com.google.gson.*

fun JsonParser.parseAsJsonObject(json: String) = parse(json) as JsonObject