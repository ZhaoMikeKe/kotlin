package com.huachu.myapplication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.huachu.myapplication.R
import com.huachu.myapplication.adapter.DemoViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_tablayout.*
import kotlinx.android.synthetic.main.activity_tablayout.viewpager
import com.huachu.myapplication.adapter.ViewPagerFragmentAdapter


class TablayoutActivity : AppCompatActivity() {

    private val arrayList = ArrayList<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tablayout)
        viewpager.adapter = ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle())

        TabLayoutMediator(tabs, viewpager, TabLayoutMediator.OnConfigureTabCallback { tab, position ->
            // Styling each tab here
            tab.text = "Tab $position"
        }).attach()
        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        ToastUtils.showShort("0")
                    }
                    1 -> {
                        ToastUtils.showShort("1")
                    }
                    2 -> {
                        ToastUtils.showShort("2")
                    }
                }
            }
        })
    }
}

