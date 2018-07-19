package one.caiye.micropay

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.danneu.result.Result
import kotlinx.android.synthetic.main.fragment_traderecord_item.view.*
import kotlinx.android.synthetic.main.fragment_traderecord_list.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.IOException
import java.net.SocketTimeoutException

class TradeRecordFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val transferRecord: MutableList<Record> = ArrayList()

    private lateinit var username: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString("username")
        }

        api = (activity as MainActivity).api
    }

    override fun onRefresh() {
        transferRecord.clear()

        async(UI) {
            try {
                val records = bg { api.getRecords() }.await()
                when (records) {
                    is Result.Ok -> {
                        Log.d(TAG, "records length is ${records.value.payload.size}")
                        for (rec in records.value.payload) {
                            Log.d(TAG, "Adding $rec to list")
                            transferRecord.add(rec)
                        }
                        list.adapter.notifyDataSetChanged()
                    }
                    is Result.Err -> {
                        val msg = "Failed to retrieve transaction records: ${records.error.message}"
                        Log.d(TAG, msg)
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
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
        swiperefresh.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_traderecord_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set the adapter
        if (list is RecyclerView) {
            with(list) {

                layoutManager = LinearLayoutManager(context)
                Log.d(TAG, "about to at recyclerView adapter")
                if (adapter == null) {
                    Log.d(TAG, "adapter is null")
                    adapter = MyTradeRecordRecyclerViewAdapter(transferRecord, username)
                }
            }
        } else {
            Log.d(TAG, "not recycleView")
        }

        swiperefresh.setOnRefreshListener(this)
        onRefresh()
    }

    companion object {

        private const val TAG = "TradeRecordFragment"
        private lateinit var api: Api

        @JvmStatic
        fun newInstance(name: String) = TradeRecordFragment().apply {
            arguments = Bundle().apply {
                putString("username", name)
            }
        }


        class MyTradeRecordRecyclerViewAdapter(
                private val mValues: List<Record>, private val username: String)
            : RecyclerView.Adapter<MyTradeRecordRecyclerViewAdapter.ViewHolder>() {


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.fragment_traderecord_item, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val item = mValues[position]
                Log.d("RENDER_ITEM", "rendering ${item.username_from} ${item.username_to} ${item.amount}")
                holder.timeView.text = item.time
                holder.objectView.text = if (item.username_from == username) "to ${item.username_to}" else "from ${item.username_from}"
                holder.moneyView.text = "${item.amount}"
                holder.moneyView.setTextColor(if (item.username_from != username) Color.RED else Color.BLACK)

                with(holder.mView) {
                    tag = item
                }
            }

            override fun getItemCount(): Int = mValues.size

            inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
                val timeView: TextView = mView.time
                val objectView: TextView = mView.`object`
                val moneyView: TextView = mView.money

            }
        }
    }
}
