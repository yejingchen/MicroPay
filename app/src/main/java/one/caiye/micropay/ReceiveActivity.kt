package one.caiye.micropay

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_receive.*

class ReceiveActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback {

    private var moneyAmount: String? = null
    private var username: String? = null
    private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)

        username = intent.getStringExtra("username")
        moneyAmount = intent.getStringExtra("money")
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        mNfcAdapter!!.setNdefPushMessageCallback(this, this)
        receiveTextView.text = getString(R.string.receive_waiting_hint)
    }

    override fun createNdefMessage(p0: NfcEvent?): NdefMessage {
        val text = "$username%$moneyAmount"
        return NdefMessage(arrayOf(
                NdefRecord.createMime("pay/vnd.micropay.caiye", text.toByteArray())
        ))
    }


}
