package one.caiye.micropay

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.danneu.result.Result
import kotlinx.android.synthetic.main.fragment_transfer.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.IOException
import java.net.SocketTimeoutException

class TransferFragment : Fragment() {
    private var username: String? = null
    private lateinit var api: Api

    companion object {
        const val TAG = "TransferFragment"
        @JvmStatic
        fun newInstance(name: String) = TransferFragment().apply {
            arguments = Bundle().apply {
                putString("username", name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        api = (activity as MainActivity).api

        arguments?.let {
            username = it.getString("username")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_transfer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nfc_transfer_button.setOnClickListener {
            val intent = Intent(activity, TransferActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }
        transfer_button.setOnClickListener {
            val receiver = receiver_name.text.toString()
            val amount = transfer_amount.text.toString()

            // no need to check password
//            val password = transfer_password.text.toString()
            Toast.makeText(activity, "Transfering ￥$amount to $receiver", Toast.LENGTH_LONG).show()

            async(UI) {
                try {
                    val result = bg {
                        api.initiateTransaction(username!!,
                                receiver, amount.toDouble())
                    }.await()
                    when (result) {
                        is Result.Ok -> {
                            transfer_amount.text.clear()
                            val msg = "Transferred $amount to $receiver"
                            Log.d(TAG, msg)
                            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                        }
                        is Result.Err -> {
                            val msg = "Failed to transfer: ${result.error.message}"
                            Log.d(TAG, msg)
                            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                    transfer_password.text.clear()
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
}
