package io.github.aughtone.types.util


fun bitSet(): BitSet = BitSet()
fun bitSet(size: Int): BitSet = BitSet(size = size)
fun bitSet(size: Int, initializer: (Int) -> Boolean): BitSet =
    BitSet(size = size, initializer = initializer)

fun bitSet(size: Int, all: Boolean): BitSet = BitSet(size = size, initializer = { all })
