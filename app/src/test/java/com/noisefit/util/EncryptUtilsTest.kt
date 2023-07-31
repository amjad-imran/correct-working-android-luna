package com.noisefit.util

import com.google.common.truth.Truth
import com.noisefit_commans.utils.EncryptUtils
import org.junit.Test


class EncryptUtilsTest {

    @Test
    fun `encrypt data success`() {
        val valueToEncrypt = "test"
        val expectedResult = "098f6bcd4621d373cade4e832627b4f6"
        val result = EncryptUtils().md5(valueToEncrypt)
        Truth.assertThat(result).isEqualTo(expectedResult)
    }

}