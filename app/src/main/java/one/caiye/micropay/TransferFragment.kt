package one.caiye.micropay

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_transfer.*

class TransferFragment : Fragment() {
    private var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val receiver = receiver_name.text.toString();
            val amount = transfer_amount.text.toString();
            val password = transfer_password.text.toString();
            Toast.makeText(activity, "Transfering ￥$amount to $receiver", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(name: String) = TransferFragment().apply {
            arguments = Bundle().apply {
                putString("username", name)
            }
        }
    }
}
