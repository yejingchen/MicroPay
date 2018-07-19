package one.caiye.micropay

import android.content.Context
import android.util.Log
import com.danneu.result.Result
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.IOException

class UnexpectedAPIRespException(msg: String, val body: String?) : Exception(msg)

class Api(val context: Context) {

    companion object {
        const val TAG = "API"
        const val MICROPAY_API = "http://123.207.220.103:7001"
    }

    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    private val cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
    private val httpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()

    // UTILITIES

    private fun query(url: String, method: String = "GET", body: RequestBody? = null): String {
        Log.d(TAG, "querying $url")
        val req = Request.Builder()
                .url(url)
                .method(method, body)
                .build()
        val resp = httpClient.newCall(req)
                .execute()
        if (!resp.isSuccessful) {
            throw IOException("Unexpected code $resp")
        }
        if (resp.body() == null) {
            // empty response, we can do nothing here
            throw UnexpectedAPIRespException("Empty API response", null)
        }
        val respBody = resp.body()!!.string()

        /*
        val headers = resp.headers()
        Log.d("HEADER", "RESP headers (count: ${headers.size()})")
        for (i in 0 until headers.size()) {
            Log.d("HEADER", "${headers.name(i)}: ${headers.value(i)}")
        }
        */
        Log.d(TAG, "query: response is $respBody")
        return respBody
    }

    private fun parseErrorResponse(json: String): Result.Err<ErrorResponse> {
        val errorAdapter = moshi.adapter(ErrorResponse::class.java)
        val error: ErrorResponse
        try {
            error = errorAdapter.fromJson(json)!!
        } catch (e: JsonDataException) {
            Log.e(TAG, e.toString())
            throw UnexpectedAPIRespException("Unexpected API response: ${e.message}", json)
        }
        Log.d(TAG, "parseErrorResponse: Result.Err<ErrorResponse>: $error")
        return Result.err(error)
    }

    /**
     * @param err Bangumi API has a bug with certain calls, if the specified username doesn't exist,
     * the web API will return a HTML page instead of JSON data.
     * As a workaround, pass a Err<ErrorResponse> (usually a 404 message) to [err] to prevent
     * UnexpectedAPIRespException from being thrown, and return [err] instead.
     */
    private inline fun <reified T> parseJsonArray(json: String, err: Result.Err<ErrorResponse>? = null):
            Result<List<T>, ErrorResponse> {

        return try {
            val type = Types.newParameterizedType(List::class.java, T::class.java)
            val adapter = moshi.adapter<List<T>>(type)
            val list = adapter.fromJson(json)!!
            Result.ok(list)
        } catch (e: JsonDataException) {
            parseErrorResponse(json)
        } catch (e: IOException) {
            err ?: throw UnexpectedAPIRespException("Malformed API response", json)
        }
    }

    /**
     * @param err see [parseJsonArray]
     */
    private inline fun <reified T> parseJson(json: String, err: Result.Err<ErrorResponse>? = null):
            Result<T, ErrorResponse> {

        return try {
            val adapter = moshi.adapter(T::class.java)
            val data = adapter.fromJson(json)!!
            Result.ok(data)
        } catch (e: JsonDataException) {
            Log.d(TAG, "parseJson: maybe a ErrorResponse")
            return parseErrorResponse(json)
        } catch (e: IOException) {
            err ?: throw UnexpectedAPIRespException("Malformed API response", json)
        } catch (e: Exception) {
            Log.wtf(TAG, "parseJson: Exception $e")
            e.printStackTrace()
            err!!
        }
    }

    // APIs

    fun login(username: String, password: String): Result<MPResponse, ErrorResponse> {
        val formbody = FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build()
        val json = query("$MICROPAY_API/api/user/login", "POST", formbody)

        return parseJson(json)
    }

    fun getAccountInfo(): Result<AccountResponse, ErrorResponse> {
        val json = query("$MICROPAY_API/api/account")
        return parseJson(json)
    }

    fun initiateTransaction(from: String, to: String, amount: Double):
            Result<TransactionResp, ErrorResponse> {
        val formbody = FormBody.Builder()
                .add("username_from", from)
                .add("username_to", to)
                .add("amount", "$amount")
                .build()
        val json = query("$MICROPAY_API/api/transaction", "POST", formbody)
        return parseJson(json)
    }

    fun getRecords(): Result<RecordsResponse, ErrorResponse> {
        val json = query("$MICROPAY_API/api/record")
        return parseJson(json)
    }
}
