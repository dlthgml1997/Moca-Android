package com.example.parkseeun.moca_android.ui.main

import android.content.Intent
import com.example.parkseeun.moca_android.ui.ranking.RankingActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.example.parkseeun.moca_android.ui.mypage.NavigationActivity
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.model.get.GetHomeHotplaceResponse
import com.example.parkseeun.moca_android.model.get.GetMocaplusResponse
import com.example.parkseeun.moca_android.model.get.HomeHotplaceData
import com.example.parkseeun.moca_android.model.get.MocaplusData
import com.example.parkseeun.moca_android.ui.mocapicks.MocaPicksListActivity
import com.example.parkseeun.moca_android.ui.plus.PlusActivity
import com.example.parkseeun.moca_android.util.SharedPreferenceController
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.app_bar_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity2 : NavigationActivity(), View.OnClickListener {
    private val TAG = "HomeActivity"

    private val pickposts: ArrayList<String> = ArrayList()
    val hotList: ArrayList<HomeHotplaceData>by lazy {
        ArrayList<HomeHotplaceData>()
    }
    val rankingPosts: ArrayList<CategoryRankData> = ArrayList<CategoryRankData>()
    val plusData: ArrayList<MocaplusData> = ArrayList<MocaplusData>()


    lateinit var homeHotplaceAdapter: HomeHotplaceAdapter
    lateinit var homeMocaplusAdapter: HomeMocaplusAdapter


    override fun onClick(v: View?) {
        when (v) {
            home_picks_tv -> {
                startActivity(Intent(this, MocaPicksListActivity::class.java))
            }
            home_ranking_tv -> {
                startActivity(Intent(this, RankingActivity::class.java))
            }
            home_plus_tv -> {
                startActivity(Intent(this, PlusActivity::class.java))
            }
            home_menu_iv -> {
                drawer_layout.openDrawer(nav_view)
            }
        }
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (p0.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }

        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)

        recyclerView()

        setHomeNetwork()

        home_picks_tv.setOnClickListener(this)
        home_ranking_tv.setOnClickListener(this)
        home_plus_tv.setOnClickListener(this)
        home_menu_iv.setOnClickListener(this)

        makeData()





        setHeader(nav_view)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rv_act_home_picks)

        // 수민 추가 (홈에서 검색 화면으로)
        home_search_iv.setOnClickListener {
            val intent: Intent = Intent(this@HomeActivity2, SearchActivity::class.java)

            startActivity(intent)
        }
//        val toggle = ActionBarDrawerToggle(
//            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
//        )
//        drawer_layout.addDrawerListener(toggle)
//        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

    }

    private fun setHomeNetwork() {
        getHomeHotplaceResponse()

        getHomeMocaplusResponse()
    }

    private fun recyclerView() {
        //cafecloud pick
        rv_act_home_picks.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_act_home_picks.adapter = CategoryPickAdapter(this, pickposts)

        //hotplace
        homeHotplaceAdapter = HomeHotplaceAdapter(this, hotList)
        rv_act_home_concept.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_act_home_concept.adapter = homeHotplaceAdapter

        //ranking
        rv_act_home_ranking.layoutManager = LinearLayoutManager(this)
        rv_act_home_ranking.adapter = CategoryRankingAdapter(this, rankingPosts)

        //plus
        homeMocaplusAdapter = HomeMocaplusAdapter(this, plusData)
        rv_act_home_plus.layoutManager = LinearLayoutManager(this)
        rv_act_home_plus.adapter = homeMocaplusAdapter

        //
    }

    private fun makeData() {
        for (i in 1..12) {
            pickposts.add("카페 $i")
        }
        for (i in 1..3) {
            rankingPosts.add(CategoryRankData("cafe $i", "location $i"))


        }
    }

    private fun getHomeHotplaceResponse() {
        val token = SharedPreferenceController.getAuthorization(this)
        val getHomeHotplaceResponse = networkService.getHomeHotplaceResponse("application/json", token)

        getHomeHotplaceResponse.enqueue(object : Callback<GetHomeHotplaceResponse> {
            override fun onFailure(call: Call<GetHomeHotplaceResponse>, t: Throwable) {
                Log.e("Hotplace load failed", t.toString())
            }

            override fun onResponse(call: Call<GetHomeHotplaceResponse>, response: Response<GetHomeHotplaceResponse>) {
                if (response.isSuccessful) {
                    Log.v("hotplace", "load")
                    val temp: ArrayList<HomeHotplaceData> = response.body()!!.data
                    if (temp.size > 0) {
                        val position = homeHotplaceAdapter.itemCount
                        homeHotplaceAdapter.dataList.addAll(temp)
                        homeHotplaceAdapter.notifyItemInserted(position)
                    }

                }
            }
        })
    }

    private fun getHomeMocaplusResponse() {
        val getHomeMocaplusResponse = networkService.getMocaplusResponse(3)

        getHomeMocaplusResponse.enqueue(object : Callback<GetMocaplusResponse> {
            override fun onFailure(call: Call<GetMocaplusResponse>, t: Throwable) {
                Log.e(TAG, "getHomeMocaplusResponse failed")
            }

            override fun onResponse(call: Call<GetMocaplusResponse>, response: Response<GetMocaplusResponse>) {
                if (response.isSuccessful) {
                    Log.v(TAG, "getHomeMocaplusResponse success")
                    val temp: ArrayList<MocaplusData> = response.body()!!.data

                    if (temp != null) {
                        if (temp.size > 0) {
                            if (temp.size >= 3) {
                                for (i in 0..2) {
                                    homeMocaplusAdapter.dataList.add(temp[i])
                                }
                            } else if (temp.size < 3) {
                                homeMocaplusAdapter.dataList.addAll(temp)
                            }
                        }
                        Log.v("plus data num", temp.size.toString())
                        val position = homeMocaplusAdapter.itemCount
                        homeMocaplusAdapter.notifyItemInserted(position)
                    }


                }
            }
        })
    }
}