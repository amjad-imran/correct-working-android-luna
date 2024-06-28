//package com.noisefit.ui.commans
//
//import com.google.common.truth.Truth
//import com.noisefit_commans.utils.prettyCount
//import com.noisefit_commans.common.convertMinuteIntoSeconds
//import com.noisefit_commans.common.roundDownDecimal
//import com.noisefit_commans.common.roundUpDecimal
//import com.noisefit_commans.common.upToNDecimal
//import com.noisefit_commans.utils.prettyCountDecimal
//import org.junit.Test
//
//class ExtensionsKtTest{
//
//    @Test
//    fun `null time return zero`() {
//        val time: Int? = null
//        val result = time.convertMinuteIntoSeconds()
//        Truth.assertThat(result).isEqualTo(0)
//    }
//
//    @Test
//    fun `minute time return 60`() {
//        val time = 1
//        val result = time.convertMinuteIntoSeconds()
//        Truth.assertThat(result).isEqualTo(60)
//    }
//
//    @Test
//    fun `round down decimal`() {
//        val value = 10.677
//        val result = value.roundDownDecimal()
//        Truth.assertThat(result).isEqualTo("10.67")
//    }
//
//    @Test
//    fun `round up decimal`() {
//        val value = 10.677
//        val result = value.roundUpDecimal()
//        Truth.assertThat(result).isEqualTo("10.68")
//    }
//
//
//    @Test
//    fun `number formatter k`() {
//        val value = 1256
//        val result = value.prettyCount()
//        Truth.assertThat(result).isEqualTo("1K")
//    }
//
//    @Test
//    fun `number formatter M`() {
//        val value = 1256565
//        val result = value.prettyCount()
//        Truth.assertThat(result).isEqualTo("1M")
//    }
//
//
//    @Test
//    fun `number formatter K decimal 1 place`() {
//        val value = 100000
//        val result = value.prettyCountDecimal()
//        Truth.assertThat(result).isEqualTo("100K")
//    }
//
//    @Test
//    fun `number formatter K decimal 1 place with 1k`() {
//        val value = 100100
//        val result = value.prettyCountDecimal()
//        Truth.assertThat(result).isEqualTo("100.1K")
//    }
//
//    @Test
//    fun `upToNDecimal() test with no 2 places`() {
//        val value = 103.563534f
//        val result = value.upToNDecimal(2)
//        Truth.assertThat(result).isEqualTo("103.56")
//    }
//
//    @Test
//    fun `upToNDecimal() test with no 2 places up`() {
//        val value = 103.568534f
//        val result = value.upToNDecimal(2)
//        Truth.assertThat(result).isEqualTo("103.57")
//    }
//    @Test
//    fun `upToNDecimal() test with no 0`() {
//        val value = 103f
//        val result = value.upToNDecimal(2).replace(".00","")
//        Truth.assertThat(result).isEqualTo("103")
//    }
//
//
//}