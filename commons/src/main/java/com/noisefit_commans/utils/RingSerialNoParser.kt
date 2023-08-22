package com.noisefit_commans.utils


class RingSerialNoParser {


    /** 52014E0417130001
     * 52 01 4E 04 17 13 0001
     * R 1 N 04 23 19 00256
     * R 1 N 04 23 19 00256
     */
    fun convertSerialNo(serialNo: String): String {
        try {
            val text1: String =
                (serialNo.substring(0, 2).decodeHex()[0].toInt().toChar()) + ""
            val text2: String =
                (serialNo.substring(2, 4)).decodeHex().byteToInt().toString() + ""
            val text3: String =
                (serialNo.substring(4, 6)).decodeHex()[0].toInt().toChar() + ""
            val text4: String =
                (serialNo.substring(6, 8)).decodeHex().byteToInt().toString() + ""
            val text5: String =
                (serialNo.substring(8, 10)).decodeHex().byteToInt().toString() + ""
            val text6: String =
                (serialNo.substring(10, 12)).decodeHex().byteToInt().toString() + ""
            val text7: String =
                ((serialNo.substring(12, 16)).decodeHex()).byteToInt().toString() + ""
            return (text1 + text2 + text3 + repairZero(text4, 2)
                    + repairZero(text5, 2) + repairZero(text6, 2) + repairZero(text7, 5))
        } catch (exp: Exception) {
            return ""
        }
    }

    private fun ByteArray.byteToInt(): Int {
        var result = 0
        var shift = 0
        for (byte in this) {
            result = result or (byte.toInt() shl shift)
            shift += 8
        }
        return result
    }



    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }


    private fun repairZero(text: String, max: Int): String {
        return if (text.length < max) {
            val a = StringBuilder()
            for (i in 0 until max - text.length) {
                a.append("0")
            }
            StringBuilder(a.toString() + text).toString()
        } else {
            text
        }
    }

    /**
     * R1N04230100001
     * @return Pair<RingColor,Size>
     */
    fun getRingColorAndSize(): Pair<RingColor, Int>? {
        return try {
            when ("R1N04230100001".substring(7, 9).toInt()) {
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