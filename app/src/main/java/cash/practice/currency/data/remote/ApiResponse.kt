package cash.practice.currency.data.remote

import android.util.Log
import retrofit2.Response
import java.io.IOException

/**
 * Common class used by API responses.
 * @param <T>
</T> */
private const val TAG = "ApiResponse"
class ApiResponse<T> {

    val code: Int

    val body: T?

    val errorMessage: String?

    val isHttpSuccess: Boolean
        get() = code in 200..299

    constructor(error: Throwable) {
        code = 500
        body = null
        errorMessage = error.message
    }

    constructor(response: Response<T>) {
        code = response.code()
        if (isHttpSuccess) {
            body = response.body()
            errorMessage = null
        } else {
            var message: String? = null
            if (response.errorBody() != null) {
                try {
                    message = response.errorBody()?.string()
                } catch (ignored: IOException) {
                    Log.e(TAG, "error while parsing response: " + Log.getStackTraceString(ignored))
                }
            }
            if (message == null || message.trim { it <= ' ' }.isEmpty()) {
                message = response.message()
            }
            errorMessage = message
            body = null
        }
    }

    override fun toString(): String {
        return "code = $code, body = $body, error message = $errorMessage"
    }

}