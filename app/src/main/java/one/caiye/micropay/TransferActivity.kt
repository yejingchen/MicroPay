package one.caiye.micropay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.danneu.result.Result
import kotlinx.android.synthetic.main.activity_transfer.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.IOException
import java.net.SocketTimeoutException

class TransferActivity : AppCompatActivity() {

    private var username: String? = null
    private lateinit var api: Api

    companion object {
        const val TAG = "TransferActivity"
    }

    private lateinit var mNfcAdapter: NfcAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)
        val tmp: NfcAdapter? = NfcAdapter.getDefaultAdapter(this)
        api = Api(this)

        if (tmp == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        mNfcAdapter = tmp
        mNfcAdapter.setNdefPushMessage(null, this)

        username = intent.getStringExtra("username")
    }


    override fun onResume() {
        super.onResume()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processIntent(intent)
        }
    }

    private fun processIntent(intent: Intent?) {
        val rawMsgs = intent?.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        val msg = rawMsgs?.get(0) as NdefMessage
        receive_textView.text = getString(R.string.nfc_transfer_waiting)
        val seq = String(msg.records[0].payload).splitToSequence("%")
        val receiver = seq.first()
        val money = seq.last()

        sendServerMessage(receiver, money)
    }

    private fun sendServerMessage(receiver: String, amount: String) {
        showProgress(true)

        async(UI) {
            try {
                val result = bg {
                    api.initiateTransaction(username!!,
                            receiver, amount.toDouble())
                }.await()
                when (result) {
                    is Result.Ok -> {
                        val msg = "Transferred $amount to $receiver"
                        Log.d(TAG, msg)
                        Toast.makeText(this@TransferActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                    is Result.Err -> {
                        val msg = "Failed to transfer: ${result.error.message}"
                        Log.d(TAG, msg)
                        Toast.makeText(this@TransferActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(this@TransferActivity, "Error connecting: $e", Toast.LENGTH_LONG).show()
                Log.d(TAG, e.toString())
            } catch (e: UnexpectedAPIRespException) {
                Toast.makeText(this@TransferActivity, "Unexpected API response: ${e.body}", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Unexpected API response: ${e.body}")
            } catch (e: SocketTimeoutException) {
                Toast.makeText(this@TransferActivity, "Connection timeout: $e", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun showProgress(show: Boolean) {

        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
        mReceiveProgressBar.visibility = (if (show) View.VISIBLE else View.GONE)
        mReceiveProgressBar.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mReceiveProgressBar.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }


    public override fun onNewIntent(intent: Intent) {
        setIntent(intent)
    }
}
