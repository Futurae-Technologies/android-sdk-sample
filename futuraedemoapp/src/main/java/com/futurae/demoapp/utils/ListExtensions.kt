package com.futurae.demoapp.utils

fun List<Int>.toCharArray() : CharArray{
    return this.joinToString("") { it.toString() }.toCharArray()
}