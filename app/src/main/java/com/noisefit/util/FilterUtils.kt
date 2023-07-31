package com.noisefit.util

import android.graphics.Color
import android.graphics.ColorMatrix
import com.zomato.photofilters.geometry.Point
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubFilter


class FilterUtils {

    fun getFilter(filter: String, intensityPercentage: Int): ColorMatrix? {

        val intensity = (intensityPercentage.toFloat() / 100)
        when (filter) {
            com.noisefit.ui.diy.Filter.TEAL.type -> {
                return FilterUtils().getTeal(intensity)
            }

            com.noisefit.ui.diy.Filter.RUBY.type -> {
                return FilterUtils().getRuby(intensity)
            }

            com.noisefit.ui.diy.Filter.LILAC.type -> {
                return FilterUtils().getLilac(intensity)
            }

            com.noisefit.ui.diy.Filter.OCHRE.type -> {
                return FilterUtils().getOchre(intensity)
            }

            com.noisefit.ui.diy.Filter.INDIGO.type -> {
                return FilterUtils().getIndigo(intensity)
            }
        }
        return null
    }

    fun getFilter2(hexColor: String): ColorMatrix {
        val red = Integer.parseInt(hexColor.substring(1, 3), 16) / 255f
        val green = Integer.parseInt(hexColor.substring(3, 5), 16) / 255f
        val blue = Integer.parseInt(hexColor.substring(5, 7), 16) / 255f
        val brightness = 0.5f // Adjust this value to increase or decrease brightness

        val colorMatrix = floatArrayOf(
            red * brightness, 0f, 0f, 0f, 0f,
            0f, green * brightness, 0f, 0f, 0f,
            0f, 0f, blue * brightness, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )

        return ColorMatrix(colorMatrix)
    }

    fun getFilter1(hexColor: String): ColorMatrix {
        val color = Color.parseColor(hexColor)
// Define the intensity value (a float between 0.0f and 1.0f)
// Define the intensity value (a float between 0.0f and 1.0f)
        val intensity = 0f
// Define the color matrix array
// Define the color matrix array
        val colorMatrixArray = floatArrayOf(
            (color shr 16 and 0xFF) / 255.0f, 0f, 0f, 0f, 0f,  // Red
            0f, (color shr 8 and 0xFF) / 255.0f, 0f, 0f, 0f,  // Green
            0f, 0f, (color and 0xFF) / 255.0f, 0f, 0f,  // Blue
            0f, 0f, 0f, 1f, 0f // Alpha
        )
// Apply the intensity value to the color matrix array
// Apply the intensity value to the color matrix array
        for (i in 0..2) {
            colorMatrixArray[i * 5] *= intensity
            colorMatrixArray[i * 5 + 1] *= 1 - intensity
            colorMatrixArray[i * 5 + 2] *= 1 - intensity
        }
// Create a new ColorMatrix object
// Create a new ColorMatrix object
        val colorMatrix = ColorMatrix(colorMatrixArray)
        return colorMatrix

    }

    fun getFilter(hexColor: String): ColorMatrix {
        // Define the hex color value as a string
        // Define the hex color value as a string
// Convert the hex color value to an integer
// Convert the hex color value to an integer
        val color = Color.parseColor(hexColor)
// Define the intensity value (a float between 0.0f and 1.0f)
// Define the intensity value (a float between 0.0f and 1.0f)
        val intensity = 1f
// Convert the color to a float array
// Convert the color to a float array
        val colorArray = floatArrayOf(
            (color shr 16 and 0xFF) / 255.0f, 0f, 0f, 0f, 0f,
            0f, (color shr 8 and 0xFF) / 255.0f, 0f, 0f, 0f,
            0f, 0f, (color and 0xFF) / 255.0f, 0f, 0f,
            0f, 0f, 0f, (color shr 24 and 0xFF) / 255.0f, 0f
        )
// Define a new array with the modified color values
// Define a new array with the modified color values
        val colorArrayModified = floatArrayOf(
            colorArray[0] * (1 - intensity),
            colorArray[1] * intensity,
            colorArray[2] * intensity,
            0f,
            0f,
            colorArray[4] * intensity,
            colorArray[5] * (1 - intensity),
            colorArray[6] * intensity,
            0f,
            0f,
            colorArray[8] * intensity,
            colorArray[9] * intensity,
            colorArray[10] * (1 - intensity),
            0f,
            0f,
            0f,
            0f,
            0f,
            colorArray[15],
            0f
        )
// Create a new color matrix with the modified color values
// Create a new color matrix with the modified color values
        return ColorMatrix(colorArrayModified)
    }

    fun getIndigo(intensity: Float): ColorMatrix {
        val red = 0.5f
        val green = 0f
        val blue = 1.2f
        // Adjust this value to increase or decrease intensity
        val colorMatrix = floatArrayOf(
            red, 0f, 0f, 0f, 0f,
            0f, green, 0f, 0f, 0f,
            0f, 0f, blue, 0f, 0f,
            0f, 0f, 0f, intensity, 0f,
        )
        return ColorMatrix(colorMatrix)
    }

    fun getOchre(intensity: Float): ColorMatrix {
        val red = 1.2f
        val green = 0.8f
        val blue = 0.6f
        // Adjust this value to increase or decrease intensity
        val colorMatrix = floatArrayOf(
            red, 0f, 0f, 0f, 0f,
            0f, green, 0f, 0f, 0f,
            0f, 0f, blue, 0f, 0f,
            0f, 0f, 0f, intensity, 0f,
        )
        return ColorMatrix(colorMatrix)
    }

    fun getLilac(intensity: Float): ColorMatrix {
        val red = 1.2f
        val green = 0.7f
        val blue = 1.2f
        // Adjust this value to increase or decrease intensity
        val colorMatrix = floatArrayOf(
            red, 0f, 0f, 0f, 0f,
            0f, green, 0f, 0f, 0f,
            0f, 0f, blue, 0f, 0f,
            0f, 0f, 0f, intensity, 0f,
        )
        return ColorMatrix(colorMatrix)
    }

    fun getRuby(intensity: Float): ColorMatrix {
        val red = 1.5f
        val green = 0.5f
        val blue = 0.5f
        // Adjust this value to increase or decrease intensity
        val colorMatrix = floatArrayOf(
            red, 0f, 0f, 0f, 0f,
            0f, green, 0f, 0f, 0f,
            0f, 0f, blue, 0f, 0f,
            0f, 0f, 0f, intensity, 0f,
        )
        return ColorMatrix(colorMatrix)
    }

    fun getTeal(intensity: Float): ColorMatrix {
        val red = 0.8f
        val green = 1.2f
        val blue = 1.2f

        val colorMatrix = floatArrayOf(
            red, 0f, 0f, 0f, 0f,
            0f, green, 0f, 0f, 0f,
            0f, 0f, blue, 0f, 0f,
            0f, 0f, 0f, intensity, 0f,
        )
        return ColorMatrix(colorMatrix)
    }

    /*
    Red : point 1: Input = 0 / Output = 0
          point 2: Input = 79 / Output = 180
          point 3: Input = 255 / Output = 255
     */
    fun getVibeFilter1(): Filter {
        val rgbKnots: Array<Point?> = arrayOfNulls(5)
        rgbKnots[0] = Point(0f, 0f)
        rgbKnots[1] = Point(79f, 180f)
        rgbKnots[2] = Point(255f, 255f)
        rgbKnots[3] = Point(79f, 180f)
        rgbKnots[4] = Point(255f, 255f)
        val redKnots: Array<Point?> = arrayOfNulls(3)
        redKnots[0] = Point(0f, 0f)
        redKnots[1] = Point(79f, 180f)
        redKnots[2] = Point(255f, 255f)

        val greenKnots: Array<Point?> = arrayOfNulls(2)
        greenKnots[0] = Point(0f, 0f)
        greenKnots[1] = Point(255f, 255f)
        val blueKnots: Array<Point?> = arrayOfNulls(3)
        blueKnots[0] = Point(0f, 0f)
        blueKnots[1] = Point(128f, 167f)
        blueKnots[2] = Point(255f, 255f)
        val filter = Filter()
        filter.addSubFilter(ToneCurveSubFilter(rgbKnots, redKnots, greenKnots, blueKnots))
        return filter
    }

    fun getBlueMessFilter(): Filter {
        val redKnots: Array<Point?> = arrayOfNulls<Point>(8)
        redKnots[0] = Point(0f, 0f)
        redKnots[1] = Point(86f, 34f)
        redKnots[2] = Point(117f, 41f)
        redKnots[3] = Point(146f, 80f)
        redKnots[4] = Point(170f, 151f)
        redKnots[5] = Point(200f, 214f)
        redKnots[6] = Point(225f, 242f)
        redKnots[7] = Point(255f, 255f)
        val filter = Filter()
        filter.addSubFilter(ToneCurveSubFilter(null, redKnots, null, null))
        filter.addSubFilter(BrightnessSubFilter(30))
        filter.addSubFilter(ContrastSubFilter(1f))
        return filter
    }

    fun getAweStruckVibeFilter(): Filter {
        val rgbKnots: Array<Point?> = arrayOfNulls(5)
        rgbKnots[0] = Point(0f, 0f)
        rgbKnots[1] = Point(80f, 43f)
        rgbKnots[2] = Point(149f, 102f)
        rgbKnots[3] = Point(201f, 173f)
        rgbKnots[4] = Point(255f, 255f)
        val redKnots: Array<Point?> = arrayOfNulls(5)
        redKnots[0] = Point(0f, 0f)
        redKnots[1] = Point(125f, 147f)
        redKnots[2] = Point(177f, 199f)
        redKnots[3] = Point(213f, 228f)
        redKnots[4] = Point(255f, 255f)
        val greenKnots: Array<Point?> = arrayOfNulls(6)
        greenKnots[0] = Point(0f, 0f)
        greenKnots[1] = Point(57f, 76f)
        greenKnots[2] = Point(103f, 130f)
        greenKnots[3] = Point(167f, 192f)
        greenKnots[4] = Point(211f, 229f)
        greenKnots[5] = Point(255f, 255f)
        val blueKnots: Array<Point?> = arrayOfNulls(7)
        blueKnots[0] = Point(0f, 0f)
        blueKnots[1] = Point(38f, 62f)
        blueKnots[2] = Point(75f, 112f)
        blueKnots[3] = Point(116f, 158f)
        blueKnots[4] = Point(171f, 204f)
        blueKnots[5] = Point(212f, 233f)
        blueKnots[6] = Point(255f, 255f)
        val filter = Filter()
        filter.addSubFilter(ToneCurveSubFilter(rgbKnots, redKnots, greenKnots, blueKnots))
        return filter
    }

    fun getLimeStutterFilter(): Filter? {
        val blueKnots: Array<Point?> = arrayOfNulls(3)
        blueKnots[0] = Point(0f, 0f)
        blueKnots[1] = Point(165f, 114f)
        blueKnots[2] = Point(255f, 255f)
        // Check whether output is null or not.
        val filter = Filter()
        filter.addSubFilter(ToneCurveSubFilter(null, null, null, blueKnots))
        return filter
    }

    fun getNightWhisperFilter(): Filter {
        val rgbKnots: Array<Point?> = arrayOfNulls(3)
        rgbKnots[0] = Point(0f, 0f)
        rgbKnots[1] = Point(174f, 109f)
        rgbKnots[2] = Point(255f, 255f)
        val redKnots: Array<Point?> = arrayOfNulls(4)
        redKnots[0] = Point(0f, 0f)
        redKnots[1] = Point(70f, 114f)
        redKnots[2] = Point(157f, 145f)
        redKnots[3] = Point(255f, 255f)
        val greenKnots: Array<Point?> = arrayOfNulls(3)
        greenKnots[0] = Point(0f, 0f)
        greenKnots[1] = Point(109f, 138f)
        greenKnots[2] = Point(255f, 255f)
        val blueKnots: Array<Point?> = arrayOfNulls(3)
        blueKnots[0] = Point(0f, 0f)
        blueKnots[1] = Point(113f, 152f)
        blueKnots[2] = Point(255f, 255f)
        val filter = Filter()
        filter.addSubFilter(ToneCurveSubFilter(rgbKnots, redKnots, greenKnots, blueKnots))
        return filter
    }

    fun getStarLitFilter(): Filter {
        val rgbKnots: Array<Point?> = arrayOfNulls(8)
        rgbKnots[0] = Point(0f, 0f)
        rgbKnots[1] = Point(34f, 6f)
        rgbKnots[2] = Point(69f, 23f)
        rgbKnots[3] = Point(100f, 58f)
        rgbKnots[4] = Point(150f, 154f)
        rgbKnots[5] = Point(176f, 196f)
        rgbKnots[6] = Point(207f, 233f)
        rgbKnots[7] = Point(255f, 255f)
        val filter = Filter()
        filter.addSubFilter(ToneCurveSubFilter(rgbKnots, null, null, null))
        return filter
    }
}