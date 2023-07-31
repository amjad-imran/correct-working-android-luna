package com.noisefit_commans.utils

class RingSerialNoParser(private val serialNo: String) {

    /**
     * R1N04230100001
     * @return Pair<RingColor,Size>
     */
    fun getRingColorAndSize(): Pair<RingColor, Int>? {
        return try {
            when (serialNo.substring(7, 9).toInt()) {
                1 -> Pair(RingColor.BLACK, 6)
                2 -> Pair(RingColor.BLACK, 7)
                3 -> Pair(RingColor.BLACK, 8)
                4 -> Pair(RingColor.BLACK, 9)
                5 -> Pair(RingColor.BLACK, 10)
                6 -> Pair(RingColor.BLACK, 11)
                7 -> Pair(RingColor.BLACK, 12)

                8 -> Pair(RingColor.SILVER, 6)
                9 -> Pair(RingColor.SILVER, 7)
                10 -> Pair(RingColor.SILVER, 8)
                11 -> Pair(RingColor.SILVER, 9)
                12 -> Pair(RingColor.SILVER, 10)
                13 -> Pair(RingColor.SILVER, 11)
                14 -> Pair(RingColor.SILVER, 12)


                15 -> Pair(RingColor.GOLD, 6)
                16 -> Pair(RingColor.GOLD, 7)
                17 -> Pair(RingColor.GOLD, 8)
                18 -> Pair(RingColor.GOLD, 9)
                19 -> Pair(RingColor.GOLD, 10)
                20 -> Pair(RingColor.GOLD, 11)
                21 -> Pair(RingColor.GOLD, 12)


                22 -> Pair(RingColor.ROSE_GOLD, 6)
                23 -> Pair(RingColor.ROSE_GOLD, 7)
                24 -> Pair(RingColor.ROSE_GOLD, 8)
                25 -> Pair(RingColor.ROSE_GOLD, 9)
                26 -> Pair(RingColor.ROSE_GOLD, 10)
                27 -> Pair(RingColor.ROSE_GOLD, 11)
                28 -> Pair(RingColor.ROSE_GOLD, 12)

                29 -> Pair(RingColor.STEALTH_BLACK, 6)
                30 -> Pair(RingColor.STEALTH_BLACK, 7)
                31 -> Pair(RingColor.STEALTH_BLACK, 8)
                32 -> Pair(RingColor.STEALTH_BLACK, 9)
                33 -> Pair(RingColor.STEALTH_BLACK, 10)
                34 -> Pair(RingColor.STEALTH_BLACK, 11)
                35 -> Pair(RingColor.STEALTH_BLACK, 12)

                else -> null
            }
        } catch (exp: Exception) {
            null
        }
    }

}

enum class RingColor {
    BLACK, SILVER, GOLD, ROSE_GOLD, STEALTH_BLACK
}