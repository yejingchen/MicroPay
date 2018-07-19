package one.caiye.micropay


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.danneu.result.Result
import kotlinx.android.synthetic.main.fragment_my.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.IOException
import java.net.SocketTimeoutException


/**
 * A simple [Fragment] subclass.
 *
 */
class MyFragment : Fragment() {
    private var username = ""

    companion object {
        private const val TAG = "MyFragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments!!.getString("username")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)


        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usernameTextView.text = username
        updateBalance()

    }

    private fun updateBalance() {
        val api = (activity as MainActivity).api

        async(UI) {
            try {
                Log.d(TAG, "Querying balance")
                val info = bg { api.getAccountInfo() }.await()
                when (info) {
                    is Result.Ok -> {
                        Log.d(TAG, "Balance is ${info.value.payload.balance}")
                        balanceValueTextView.text = "${info.value.payload.balance}"
                    }

                    is Result.Err -> {
                        val msg = info.error.message

                        Log.d(TAG, "Failed to display balance: $msg")
                        Toast.makeText(activity, "Failed to display balance: $msg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(activity, "Error connecting: $e", Toast.LENGTH_LONG).show()
                Log.d(TAG, e.toString())
            } catch (e: UnexpectedAPIRespException) {
                Toast.makeText(activity, "Unexpected API response: ${e.body}", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Unexpected API response: ${e.body}")
            } catch (e: SocketTimeoutException) {
                Toast.makeText(activity, "Connection timeout: $e", Toast.LENGTH_LONG).show()
            }
        }
    }
}