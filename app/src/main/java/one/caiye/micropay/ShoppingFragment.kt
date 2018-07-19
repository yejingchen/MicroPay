package one.caiye.micropay

import java.util.Random;
import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_shopping.*

class ShoppingFragment : Fragment(){
    private var username: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            username = it.getString("username")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_shopping, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        set_button.setOnClickListener{
            val userid = UserID.text.toString()
            val itemid = ItemID.text.toString()
            if (userid.equals("") || itemid.equals("")){
                AlertDialog.Builder(activity)
                        .setTitle("设置失败")
                        .setMessage("请正确填写信息！")
                        .setPositiveButton("确定", null)
                        .show()
            }else {
                MainActivity.shopeditor?.putString("userid", userid)
                MainActivity.shopeditor?.putString("itemid", itemid)
                MainActivity.shopeditor?.commit()
                AlertDialog.Builder(activity)
                        .setTitle("设置成功")
                        .setMessage("您可以享受一键购物了！")
                        .setPositiveButton("确定", null)
                        .show()
            }
        }
        buy_button.setOnClickListener {
            val userid = MainActivity.shoppreference?.getString("userid","")
            val itemid = MainActivity.shoppreference?.getString("itemid","")
            var order = StringBuilder(itemid)
            order.append('-')
            val rand = Random()
            for (i in 1..20){
                val randInt = rand.nextInt(10)
                order.append(randInt)
            }
            if (!itemid.equals("")) {
                AlertDialog.Builder(activity)
                        .setTitle("购物成功")
                        .setMessage("商品ID：${itemid}\n订单编号:${order}")
                        .setPositiveButton("确定", null)
                        .show()
            } else {
                AlertDialog.Builder(activity)
                        .setTitle("购失败")
                        .setMessage("您还未设置按钮")
                        .setPositiveButton("确定", null)
                        .show().show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(name: String) =
                ShoppingFragment().apply {
                    arguments = Bundle().apply {
                        putString("username", name)
                    }
                }
    }
}