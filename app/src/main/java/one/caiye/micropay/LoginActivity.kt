package one.caiye.micropay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.danneu.result.Result
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var api: Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        api = Api(this)
        setContentView(R.layout.activity_login)
        logInButton.setOnClickListener { attemptLogin() }
        showProgress(false)

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        val username = usernameTextView.text.toString()
        val password = passwordTextView.text.toString()
        showProgress(true)

        async(UI) {
            try {
                Log.d(TAG, "Logging in $username")
                val result = bg { api.login(username, password) }.await()
                when (result) {
                    is Result.Ok -> {
                        Log.d(TAG, "Login Success")

                        Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    }

                    is Result.Err -> {
                        val msg = result.error.message
                        Log.d(TAG, "Login failed: $msg")
                        Toast.makeText(this@LoginActivity, "Login failed: $msg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(this@LoginActivity, "Error connecting: $e", Toast.LENGTH_LONG).show()
                Log.d(TAG, e.toString())
            } catch (e: UnexpectedAPIRespException) {
                Toast.makeText(this@LoginActivity, "Unexpected API response: ${e.body}", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Unexpected API response: ${e.body}")
            } catch (e: SocketTimeoutException) {
                Toast.makeText(this@LoginActivity, "Connection timeout: $e", Toast.LENGTH_LONG).show()
            }
            showProgress(false)
        }
    }

    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        login_progress.visibility = (if (show) View.VISIBLE else View.GONE)
        login_progress.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                login_progress.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }


    companion object {
        private const val TAG = "LoginActivity"
    }
}
