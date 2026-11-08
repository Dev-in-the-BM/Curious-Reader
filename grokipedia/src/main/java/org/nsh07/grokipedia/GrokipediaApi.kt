package org.nsh07.grokipedia

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** Read-only endpoints used for discovering and reading Grokipedia articles. */
interface GrokipediaApi {
    @GET("api/full-text-search")
    suspend fun search(@Query("query") query: String, @Query("limit") limit: Int = 12, @Query("offset") offset: Int = 0): SearchResponse

    @GET("api/typeahead")
    suspend fun typeahead(@Query("query") query: String, @Query("limit") limit: Int = 10): TypeaheadResponse

    @GET("api/page-preview")
    suspend fun page(@Query("slug") slug: String): PageResponse

    @GET("api/list-pages")
    suspend fun listPages(@Query("limit") limit: Int = 20, @Query("offset") offset: Int = 0, @Query("category") category: String? = null): ListPagesResponse

    @GET("api/stats")
    suspend fun stats(): StatsResponse

    @GET("api/tts")
    suspend fun textToSpeechSections(@Query("slug") slug: String): TtsResponse
}

/** Create the client; baseUrl is injectable for tests and compatible deployments. */
fun createGrokipediaApi(baseUrl: String = "https://grokipedia.com/"): GrokipediaApi {
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder().baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build().create(GrokipediaApi::class.java)
}

/** Grokipedia has returned count/position fields as either JSON strings or numbers. */
object StringOrNumberSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringOrNumber", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): String = (decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content
    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}

@Serializable
data class SearchResponse(val results: List<SearchResult> = emptyList(), val totalCount: Int = 0, val searchTimeMs: Double = 0.0, val detectedLanguage: String? = null)
@Serializable
data class SearchResult(val title: String = "", val slug: String = "", val snippet: String = "", val relevanceScore: Double = 0.0, @Serializable(with = StringOrNumberSerializer::class) val viewCount: String = "0", val titleHighlights: List<String> = emptyList(), val snippetHighlights: List<String> = emptyList())
@Serializable
data class TypeaheadResponse(val results: List<SearchResult> = emptyList(), val suggestions: List<String> = emptyList()) {
    val titles: List<String> get() = results.map { it.title }.ifEmpty { suggestions }
}
@Serializable
data class PageResponse(val found: Boolean = false, val page: Page = Page())
@Serializable
data class Page(val title: String = "", val slug: String = "", val content: String = "", val description: String = "", val citations: List<Citation> = emptyList(), val images: List<ArticleImage> = emptyList(), val metadata: PageMetadata = PageMetadata(), val stats: PageStats = PageStats(), val linkedPages: LinkedPages = LinkedPages())
@Serializable
data class Citation(val id: String = "", val title: String = "", val description: String? = null, val url: String = "", val favicon: String? = null)
@Serializable
data class ArticleImage(val id: String? = null, val caption: String = "", val url: String = "", @Serializable(with = StringOrNumberSerializer::class) val position: String = "", val width: Int? = null, val height: Int? = null)
@Serializable
data class PageMetadata(val categories: List<String> = emptyList(), val lastModified: Long = 0, val version: String = "", val language: String? = null, val isRedirect: Boolean = false, val redirectTarget: String? = null, val isWithheld: Boolean = false)
@Serializable
data class PageStats(@Serializable(with = StringOrNumberSerializer::class) val totalViews: String = "0", val qualityScore: Double = 0.0)
@Serializable
data class LinkedPages(val indexedSlugs: List<String> = emptyList(), val unindexedSlugs: List<String> = emptyList())
@Serializable
data class ListPagesResponse(val pages: List<ListPage> = emptyList(), val totalCount: Int = 0, val hasMore: Boolean = false)
@Serializable
data class ListPage(val slug: String = "", val title: String = "", val description: String = "", val metadata: PageMetadata = PageMetadata(), val citations: List<Citation> = emptyList(), val images: List<ArticleImage> = emptyList())
@Serializable
data class StatsResponse(@Serializable(with = StringOrNumberSerializer::class) val totalPages: String = "0", @Serializable(with = StringOrNumberSerializer::class) val totalViews: String = "0", val avgViewsPerPage: Int = 0, @Serializable(with = StringOrNumberSerializer::class) val indexSizeBytes: String = "0", val statsTimestamp: String? = null)
@Serializable
data class TtsResponse(val slug: String = "", val sections: List<TtsSection> = emptyList())
@Serializable
data class TtsSection(val id: String = "", val title: String = "", val partCount: Int = 0)
