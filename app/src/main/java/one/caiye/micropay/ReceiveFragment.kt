package one.caiye.micropay

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_receive.*


class ReceiveFragment : Fragment() {
    private var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString("username")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_receive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recv_button.setOnClickListener {
            val moneyAmount = money.text.toString()
            if (!moneyAmount.isEmpty()) {
                val intent = Intent(activity, ReceiveActivity::class.java)
                intent.putExtra("money", moneyAmount)
                intent.putExtra("username", username)
                startActivity(intent)
            } else {
                Toast.makeText(activity, "please input money", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(name: String) =
                ReceiveFragment().apply {
                    arguments = Bundle().apply {
                        putString("username", name)
                    }
                }
    }
}
