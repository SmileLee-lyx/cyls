package com.github.salomonbrys.kotson

import com.google.gson.JsonObject
import com.google.gson.JsonParser

fun JsonParser.parseAsJsonObject(json: String) = parse(json) as JsonObject