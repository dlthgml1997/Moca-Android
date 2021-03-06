package com.example.parkseeun.moca_android.ui.community.feed

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.ui.community.review_comment.ReviewCommentActivity
import com.example.parkseeun.moca_android.ui.community.review_detail.ReviewDetailActivity
import de.hdodenhof.circleimageview.CircleImageView
import android.graphics.Point
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import com.example.parkseeun.moca_android.model.get.GetFeedResponseData
import com.example.parkseeun.moca_android.model.post.PostFollowResponse
import com.example.parkseeun.moca_android.network.ApplicationController
import com.example.parkseeun.moca_android.ui.community.feed.other_user.OtherUserActivity
import com.example.parkseeun.moca_android.util.ImageAdapter
import com.example.parkseeun.moca_android.util.User
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewRecyclerViewAdapter(val context : Context, val dataList : ArrayList<GetFeedResponseData>, val idOfContext: String="") : RecyclerView.Adapter<ReviewRecyclerViewAdapter.Holder>() {
    private val networkService  = ApplicationController.instance.networkService
    private lateinit var postLikeResponse : Call<PostFollowResponse>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.rv_item_review, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 스크린 너비에 따른 이미지 길이 설정
        val width = getScreenWidth()
        holder.pic.layoutParams.width = (width-dpToPx(41.toFloat())).toInt()
        holder.pic.layoutParams.height = holder.pic.layoutParams.width

        // image viewpager setting
        holder.page.text = "1/${dataList[position].image.size}"
        holder.pic.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(pos: Int) {
                holder.page.text = "${pos+1}/${dataList[position].image.size}"
            }
        })
        var imgs: ArrayList<String> = ArrayList()
        dataList[position].image.forEach { imgs.add(it.review_img_url?:"") }
        holder.pic.adapter = ImageAdapter(context, imgs.toTypedArray())

        // view binding
        Glide.with(context).load(dataList[position].user_img_url).into(holder.profile)
        holder.name.text = dataList[position].user_name
        // 이름과 프로필 사진 누르면 본인/타인에 따라 각자의 피드로 이동
        View.OnClickListener {
            // 현재 있는 피드의 id와 다른 경우에만 이동 가능하게
            if(dataList[position].user_id != idOfContext)
                if(dataList[position].user_id == User.user_id){
                    Intent(context, FeedActivity::class.java).apply { putExtra("myFeed", true); context.startActivity(this) }
                }else{
                    Intent(context, OtherUserActivity::class.java).apply { putExtra("user_id", dataList[position].user_id); context.startActivity(this) }
                }
        }.let {
            holder.profile.setOnClickListener(it)
            holder.name.setOnClickListener(it)
        }
        holder.rating.rating = dataList[position].review_rating.toFloat()
        if(dataList[position].like)
            holder.heart.setImageResource(R.drawable.common_heart_fill)
        else
            holder.heart.setImageResource(R.drawable.common_heart_blank)
        // 좋아요 toggle 통신
        holder.heart.setOnClickListener {
            postLikeResponse = networkService.postReviewLike(User.token, dataList[position].review_id)
            postLikeResponse.enqueue(object : Callback<PostFollowResponse> {
                override fun onFailure(call: Call<PostFollowResponse>?, t: Throwable?) {
                    context.toast(t.toString())
                }

                override fun onResponse(call: Call<PostFollowResponse>?, response: Response<PostFollowResponse>?) {
                    if(response!!.isSuccessful)
                        if (response.body()!!.status == 200) {
                            dataList[position].like = !dataList[position].like
                            if(dataList[position].like) {
                                holder.heartNum.text = (++dataList[position].like_count).toString()
                                holder.heart.setImageResource(R.drawable.common_heart_fill)
                            }else {
                                holder.heartNum.text = (--dataList[position].like_count).toString()
                                holder.heart.setImageResource(R.drawable.common_heart_blank)
                            }
                        } else {
                            context.toast(response.body()!!.status.toString() + ": " + response.body()!!.message)
                        }
                }
            })
        }
        holder.commentBtn.setOnClickListener{
            Intent(context, ReviewCommentActivity::class.java).apply {
                putExtra("review_id", dataList[position].review_id)
                context.startActivity(this)
            }
        }
        holder.more.setOnClickListener {
            Intent(context, ReviewDetailActivity::class.java).apply {
                putExtra("review_id", dataList[position].review_id)
                context.startActivity(this)
            }
        }
        holder.heartNum.text = dataList[position].like_count.toString()
        holder.commentNum.text = dataList[position].comment_count.toString()
        holder.cafeName.text = dataList[position].cafe_name
        holder.time.text = dataList[position].time
        holder.cafeLocation.text = dataList[position].cafe_address
        holder.comment.text = dataList[position].review_title
    }

    // View Holder
    inner class Holder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val profile : CircleImageView = itemView.findViewById(R.id.review_item_profile_circle) as CircleImageView
        val name : TextView = itemView.findViewById(R.id.review_item_name_tv) as TextView
        val rating : RatingBar = itemView.findViewById(R.id.review_item_rating_rating) as RatingBar
        val heart : ImageView = itemView.findViewById(R.id.review_item_heart_iv) as ImageView
        val commentBtn : ImageView = itemView.findViewById(R.id.review_item_comment_iv) as ImageView
        val pic : ViewPager = itemView.findViewById(R.id.review_item_pic_vp) as ViewPager
        val heartNum : TextView = itemView.findViewById(R.id.review_item_heart2Num_iv) as TextView
        val commentNum : TextView = itemView.findViewById(R.id.review_item_comment2_tv) as TextView
        val cafeName : TextView = itemView.findViewById(R.id.review_item_cafename_tv) as TextView
        val time : TextView = itemView.findViewById(R.id.review_item_time_tv) as TextView
        val cafeLocation : TextView = itemView.findViewById(R.id.review_item_cafelocation_tv) as TextView
        val comment : TextView = itemView.findViewById(R.id.review_item_comment_tv) as TextView
        val more : ConstraintLayout = itemView.findViewById(R.id.review_item_more_const) as ConstraintLayout
        val page : TextView = itemView.findViewById(R.id.review_item_page_tv) as TextView
    }
    private fun dpToPx(dp:Float):Float{
        return (dp * context.resources.displayMetrics.density)
    }
    private fun getScreenWidth():Int{
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }
}