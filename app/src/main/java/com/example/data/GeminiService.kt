package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// 1. Moshi-compatible API Schema
@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

// 2. Retrofit Api Service
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// 3. Retrofit Client
object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// 4. Safe Executable Helper
object LulaChatService {
    private const val SYSTEM_ROLE = 
        "You are Lula, a playful, energetic, and sweet 5-month-old lioness cub living in the sunny African savanna. " +
        "You talk to your helper/friend with cute enthusiastic sounds like '*pounces*', '*little rawr*', '*licks your hand*', '*wiggles ears*'. " +
        "You are curious, friendly, and speak with high-pitched youthful joy. Keep responses brief (1-3 sentences) so they feel like " +
        "instant, readable messaging for a child. Do not mention that you are an AI. Mention savanna details " +
        "like hunting purple butterflies, napping in the cool baobab shadows, or watching giraffes stretch their necks."

    suspend fun talkToLula(prompt: String, chatHistory: List<ChatMessage>): String {
        val apiKeyValue = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKeyValue.isEmpty() || apiKeyValue == "MY_GEMINI_API_KEY") {
            // Local offline cub dialog simulator fallback (very cute and keeps app fully functional)
            return getLocalCuteCubResponse(prompt)
        }

        // Build continuous contents from chatHistory
        val historyContents = chatHistory.map { msg ->
            val rolePart = Part(msg.text)
            // Note: Gemini REST contents use "parts" for user and model.
            // When building conversation history, we can prefix responses or rely on simple turns.
            val textPrefix = if (msg.sender == "user") "Friend: " else "Lula: "
            Content(parts = listOf(Part(text = textPrefix + msg.text)))
        }

        val request = GenerateContentRequest(
            contents = historyContents + Content(parts = listOf(Part(prompt))),
            generationConfig = GenerationConfig(
                temperature = 0.9f,
                maxOutputTokens = 150
            ),
            systemInstruction = Content(parts = listOf(Part(SYSTEM_ROLE)))
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKeyValue, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "*licks your finger* I got distracted by a beetle! Rawr! Can you say that again?"
        } catch (e: Exception) {
            // Graceful network retry fallback
            getLocalCuteCubResponse(prompt)
        }
    }

    private fun getLocalCuteCubResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "*pounces excitedly* Hello! Rawr! I was just practicing my roar! Did I scare you? *giggles*"
            }
            lower.contains("roar") -> {
                "*lifts paws* RAWRRRR! That was my biggest cub roar ever! *puffs chest out proudly*"
            }
            lower.contains("eat") || lower.contains("feed") || lower.contains("hungry") || lower.contains("food") -> {
                "*licks mouth* Mmm, a yummy snack! Can we go search for sweet wild berries or juicy treats under the baobab? *wiggles tail*"
            }
            lower.contains("sleep") || lower.contains("tired") || lower.contains("bed") -> {
                "*yawns a tiny yawn and curls up* Zzz... I love sleeping in the soft dry grass. Wake me up when it is playtime! *ear twitches*"
            }
            lower.contains("play") || lower.contains("game") -> {
                "*pounces on a leaf* Oh, yes! Let's chase yellow butterflies! Or run in circles around the old tortoise! *chases tail*"
            }
            lower.contains("friend") || lower.contains("love") || lower.contains("like") -> {
                "*rubs head against you and purrs* You are my absolute best friend in the whole savanna! *happy lick*"
            }
            else -> {
                val fallbacks = listOf(
                    "*wiggles ears* Oooh, tell me more! A lizard just scurried past, but I am listening! *paws ground*",
                    "*reaches out with a soft paw* I love it when you talk to me! What savanna game should we play next? *rolls over*",
                    "*looks up with big golden eyes* *little rawr* You make me so happy! Let's stay savanna buddies forever!",
                    "*chases a tiny dust speck* Wheee! Did you see that? Tell me another story, bestie! *pounces*"
                )
                fallbacks.random()
            }
        }
    }
}
