package com.oreo.data.model

import com.noisefit_commans.utils.LOGS
import org.json.JSONArray
import org.json.JSONObject

data class WhatsNewBlogItem(
    val title: String?,
    val imageUrl: String?,
    val description: String?
)

data class WhatsNewSection(
    val title: String,
    val description: String,
    val imageUrl: String?,
    val blogDetails: List<WhatsNewBlogItem>
)

object WhatsNewParser {
    private val SUPPORTED_LANGUAGES = setOf("en", "fr", "de", "es", "it", "nl", "zh", "pt", "th", "ru")
    private const val DEFAULT_LANGUAGE = "en"

    fun getVoiceAISection(blogPosition: Int, json: String, languageCode: String): WhatsNewSection? {
        return try {
            val resolvedLang = if (languageCode in SUPPORTED_LANGUAGES) languageCode else DEFAULT_LANGUAGE
            val root = JSONObject(json)

            val langArray: JSONArray = when {
                root.has(resolvedLang) -> root.getJSONArray(resolvedLang)
                root.has(DEFAULT_LANGUAGE) -> root.getJSONArray(DEFAULT_LANGUAGE)
                else -> return null
            }
            val section = langArray.getJSONObject(blogPosition)
            return parseSection(section)
        } catch (e: Exception) {
            LOGS.e(e)
            null
        }
    }

    private fun parseSection(sectionJson: JSONObject): WhatsNewSection {
        val blogList = mutableListOf<WhatsNewBlogItem>()
        val blogDetailsArray = sectionJson.optJSONArray("blogDetails")

        if (blogDetailsArray != null) {
            for (i in 0 until blogDetailsArray.length()) {
                val item = blogDetailsArray.getJSONObject(i)
                blogList.add(
                    WhatsNewBlogItem(
                        title = item.optString("title").takeIf { it.isNotBlank() && it != "null" },
                        imageUrl = item.optString("imageUrl").takeIf { it.isNotBlank() && it != "null" },
                        description = item.optString("description").takeIf { it.isNotBlank() }
                    )
                )
            }
        }

        return WhatsNewSection(
            title = sectionJson.optString("title", ""),
            description = sectionJson.optString("description", ""),
            imageUrl = sectionJson.optString("imageUrl", ""),
            blogDetails = blogList
        )
    }
}