package one.caiye.micropay

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var username: String
        var shoppreference: SharedPreferences? = null
        var shopeditor: SharedPreferences.Editor? = null
        const val NUM_ITEMS = 5

        class MyPageAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {

            private val tabTitles = arrayOf("我", "收款", "转账", "交易历史","购物")

            override fun getPageTitle(position: Int): CharSequence? {
                return tabTitles[position]
            }

            override fun getItem(position: Int): Fragment {
                val args = Bundle()
                args.putString("username", username)
                return when (position) {
                    0 -> {
                        val f = MyFragment()
                        f.arguments = args
                        f
                    }
                    1 -> ReceiveFragment.newInstance(username)
                    2 -> TransferFragment.newInstance(username)
                    3 -> TradeRecordFragment.newInstance(username)
                    4 -> ShoppingFragment.newInstance(username)
                    else -> {
                        val f = MyFragment()
                        f.arguments = args
                        f
                    }
                }
            }
            override fun getCount(): Int {
                return NUM_ITEMS
            }
        }
    }

    internal lateinit var api: Api

    private lateinit var mPageAdapter: MyPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        api = Api(this)

        mPageAdapter = MyPageAdapter(supportFragmentManager)

        pager.adapter = mPageAdapter

        username = intent.getStringExtra("username")
        shoppreference=getSharedPreferences("shopping", MODE_PRIVATE)
        shopeditor=shoppreference?.edit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


}


