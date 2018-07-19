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

    private inline fun <reified T> parseJson(json: String):
            Result<T, ErrorResponse> {

        return try {
            val adapter = moshi.adapter(T::class.java)
            val data = adapter.fromJson(json)!!
            Log.d(TAG, "Parsed $data")
            Result.ok(data)
        } catch (e: JsonDataException) {
            Log.d(TAG, "parseJson: maybe a ErrorResponse")
            parseErrorResponse(json)
        } catch (e: IOException) {
            throw UnexpectedAPIRespException("Malformed API response", json)
        } catch (e: Exception) {
            Log.wtf(TAG, "Exception $e")
            e.printStackTrace()
            throw e
        }
    }

    fun clearCookies() {
        cookieJar.clear()
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
