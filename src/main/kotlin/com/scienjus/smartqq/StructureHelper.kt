package com.scienjus.smartqq

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline val <reified T : Any> T.LOGGER: Logger get() = LoggerFactory.getLogger(T::class.java)