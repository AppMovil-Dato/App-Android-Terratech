package com.novatech.terratech.core.domain

class Failure(val code: String, val status: Int = 0) : Exception(code)
