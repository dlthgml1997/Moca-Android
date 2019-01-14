package com.example.parkseeun.moca_android.ui.community.review_write

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.CursorLoader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.model.post.PostReviewWriteResponse
import com.example.parkseeun.moca_android.model.post.PostWriteCommentData
import com.example.parkseeun.moca_android.model.post.PostWriteCommentResponse
import com.example.parkseeun.moca_android.network.ApplicationController
import com.example.parkseeun.moca_android.ui.community.review_write.adapter.PhotoAdapter
import com.example.parkseeun.moca_android.ui.community.review_write.data.PhotoData
import com.example.parkseeun.moca_android.ui.community.review_write.data.ReviewImageData
import com.example.parkseeun.moca_android.util.User
import kotlinx.android.synthetic.main.activity_community_writereview.*
import kotlinx.android.synthetic.main.activity_review_detail.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.textColorResource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class WriteReviewActivity : AppCompatActivity(), TextWatcher {
    //    var PICK_IMAGE_MULTIPLE = 1
//    var imageEncoded: String? = null
//    var imagesEncodedList = ArrayList<String>()
    private val My_READ_STORAGE_REQUEST_CODE = 1004
    private val REQ_CODE_SELECT_IMAGE = 100
    lateinit var data: Uri
    lateinit var imageURI : String
    var REQUEST_CODE: Int = 1007
    private var mImage: MultipartBody.Part? = null
    var btn_num = 0
    private var cafe_id: Int? = null
    private var currentProgress: Int = 0

    //통신
    private val networkService by lazy {
        ApplicationController.instance.networkService
    }

    lateinit var postReviewWriteResponse: Call<PostReviewWriteResponse>
    var images = ArrayList<MultipartBody.Part>()
    lateinit var photoItems: ArrayList<PhotoData>
    lateinit var totalItems: ArrayList<PhotoData>
    lateinit var PhotoAdapter: PhotoAdapter
    lateinit var requestManager: RequestManager
    lateinit var reviewImageItems: ArrayList<ReviewImageData>  //서버로 보낼 이미지 스트링 배열

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_writereview)
        reviewImageItems = ArrayList()

        requestManager = Glide.with(this)
        photoItems = ArrayList()

        var displayMetrics = applicationContext.resources.displayMetrics //화면 해상도를 구함
        //var width = displayMetrics.widthPixels
        //var height = displayMetrics.heightPixels
        Log.v("onCreate", "아아")
        setUnderlineColor()
        SetOnClickListener()
        ratingBar()
        whenFromDetailActivity()
        et_addreview_cafename.addTextChangedListener(this)
        et_addreview_cafeaddress.addTextChangedListener(this)
        et_addreview_oneline.addTextChangedListener(this)
        et_addreview_multiline.addTextChangedListener(this)
    }

    private fun whenFromDetailActivity() {
        var cafe_id_default = intent.getIntExtra("cafe_id_default", 0)
        var cafename = intent.getStringExtra("cafename")
        var cafeaddress = intent.getStringExtra("cafeaddress")
        Log.v("디테일에서 넘어온거", cafe_id_default.toString() + " " + cafename + " " + cafeaddress)
        if (cafename != null) {
            cafe_id = cafe_id_default
            Log.v("디테일에서 넘어온 거 넣음 ", cafe_id.toString())
            et_addreview_cafename.text = cafename
            et_addreview_cafeaddress.text = cafeaddress
            et_addreview_cafename.isEnabled = false
            et_addreview_cafename.isClickable = false
        }
    }

    private fun setUnderlineColor() {
        //카페이름
        et_addreview_cafename.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_cafename.text.toString().isNotEmpty()) {
                    v_write_review_cafename.backgroundResource = R.color.point_pink
                } else {
                    v_write_review_cafename.backgroundResource = R.color.dark_gray
                }
            }
        }
        //카페주소
        et_addreview_cafeaddress.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_cafeaddress.text.toString().isNotEmpty())
                    v_write_review_cafeaddress.backgroundResource = R.color.point_pink
                else v_write_review_cafeaddress.backgroundResource = R.color.dark_gray
            }
        }
        //레이팅
        if (ratingBarCustom.rating >= 0) v_write_review_rating.backgroundResource = R.color.point_pink

        //한줄 설명
        et_addreview_oneline.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_oneline.text.toString().isNotEmpty())
                    v_write_review_oneline.backgroundResource = R.color.point_pink
                else v_write_review_oneline.backgroundResource = R.color.dark_gray
            }
        }

        //상세 설명
        et_addreview_multiline.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_multiline.text.toString().isNotEmpty())
                    v_write_review_multiline.backgroundResource = R.color.point_pink
                else v_write_review_multiline.backgroundResource = R.color.dark_gray
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1007 -> {
                    et_addreview_cafename.text = data!!.getStringExtra("cafe_name")
                    cafe_id = data!!.getIntExtra("cafe_id", 0)
                    et_addreview_cafeaddress.text = data!!.getStringExtra("cafe_address")
                }
            }
        }
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    if(data != null){
                        val selectedImageUri : Uri = data.data
                        imageURI = getRealPathFromURI(selectedImageUri)
                    }
                    //if(ApplicationController.getInstance().is)
                    this.data = data!!.data //그이미지의Uri를 가져옴
                    Log.v("이미지1", this.data.toString())
                    val options = BitmapFactory.Options()
                    var input: InputStream? = null // here, you need to get your context.
                    try {
                        input = contentResolver.openInputStream(this.data)
                        Log.v("이미지", this.data.toString())
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }

                    val bitmap = BitmapFactory.decodeStream(input, null, options) // InputStream 으로부터 Bitmap 을 만들어 준다.
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)

                    //이미지 파일을 서버로 보내기 위한 코드들
                    val file = File(imageURI) // 가져온 파일의 이름을 알아내려고 사용합니다
                    val requestfile = RequestBody.create(MediaType.parse("multipart/form-data"), file)//baos.toByteArray()
                    //RequestBody photoBody = RequestBody.create(MediaType.parse("image/jpg"), baos.toByteArray());
                    // MultipartBody.Part 실제 파일의 이름을 보내기 위해 사용!!
                    images.add(MultipartBody.Part.createFormData("image", file.name, requestfile))//여기의 image는 키값의 이름하고 같아야함
                    Log.v("images size", images.size.toString())
                    //body = MultipartBody.Part.createFormData("image", photo.getName(), profile_pic);

                    photoItems.add(PhotoData(data.data))
                    reviewImageItems.add(ReviewImageData(data.data.toString()))
                    PhotoAdapter =
                            PhotoAdapter(photoItems, requestManager, images!!, this.findViewById(R.id.rl_all_addreview))
                    rv_photo_review.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    rv_photo_review.adapter = PhotoAdapter
                    setBtnEnable()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun changeImage() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)
    }

    fun ratingBar() {
        val ratingBarCustom = findViewById<View>(R.id.ratingBarCustom) as RatingBar
        ratingBarCustom.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
            var ratingBarCustomInfo = findViewById<View>(R.id.ratingBarCustomInfo) as TextView
            override fun onRatingChanged(ratingBar: RatingBar, ratingValue: Float, fromUser: Boolean) {
                currentProgress = ratingBarCustom.progress
                ratingBarCustomInfo.text = "Current start number is $ratingValue, Progress number is $currentProgress"
            }
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        setBtnEnable()
    }

    fun SetOnClickListener() {
        et_addreview_cafename.setOnClickListener {
            val intent = Intent(this, ReviewSearchLocationActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
        img_addreview_cancel.setOnClickListener {
            finish()
        }

        img_addreview_image.setOnClickListener {
            requestReadExternalStoragePermission()
        }

        val input_cafeid = cafe_id
        val input_title = et_addreview_oneline.text.toString()//한줄 설명
        val input_content = et_addreview_multiline.text.toString() //상세 설명
        val input_rating = currentProgress// 별점
        var flag: Boolean = false
        Log.v(
            "postReviewWriteResponse",
            cafe_id.toString() + "  " + input_rating.toString() + " " + input_title + " " + input_content + " " + images.toString()
        )

        if (input_title.isNotEmpty() && input_content.isNotEmpty() && input_cafeid != null && images.size > 0) { //Multipart 형식은 String을 RequestBody 타입으로 바꿔줘야 합니다
            flag = true
            changeButtonColor(flag)
            Log.v("postReviewWriteResponse", "다 널 아니다")
            img_addreview_complete.setOnClickListener {
                postReviewWriteResponse(input_cafeid, input_content, input_rating, input_title)
            finish()
            }
        } else {
            flag = false
            changeButtonColor(flag)
        }

    }

    private fun requestReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    My_READ_STORAGE_REQUEST_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    My_READ_STORAGE_REQUEST_CODE
                )
            }
        } else {

            changeImage()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == My_READ_STORAGE_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeImage()
            } else {

            }
        }
    }

    private fun changeButtonColor(flag: Boolean) {
        if (flag == true) {
            img_addreview_complete.setBackgroundResource(R.drawable.round_square_point_pink)
            img_addreview_complete.textColorResource = R.color.white
        } else {
            img_addreview_complete.setBackgroundResource(R.drawable.round_square_lightgray)
            img_addreview_complete.textColorResource = R.color.dark_gray
        }
    }


    private fun postReviewWriteResponse(
        input_cafeid: Int,
        input_content: String,
        input_rating: Int,
        input_title: String
    ) {

        var content = RequestBody.create(MediaType.parse("text/plain"), input_content)
        var title = RequestBody.create(MediaType.parse("text/plain"), input_title)

        Log.v(
            "postReviewWriteResponse",
            User.token + " " + input_cafeid.toString() + " " + title.toString() + " " + content.toString() + " " + input_rating.toString() + " " + images.toString()
        )
        postReviewWriteResponse =
                networkService.postReviewWriteResponse(User.token, input_cafeid, title, content, input_rating, images)
        postReviewWriteResponse.enqueue(object : Callback<PostReviewWriteResponse> {
            override fun onFailure(call: Call<PostReviewWriteResponse>?, t: Throwable?) {
                Log.v("postReviewWriteResponse", t.toString())
            }

            override fun onResponse(
                call: Call<PostReviewWriteResponse>?,
                response: Response<PostReviewWriteResponse>?
            ) {
                if (response!!.isSuccessful) {
                    if (response.body()!!.status == 201) {
                        finish()
                        Log.v("postReviewWriteResponse", response.body()!!.message)
                    } else {
                        Log.v("postReviewWriteResponse", response.body()!!.message)
                    }
                    Log.v("postReviewWriteResponse",response.body()!!.status.toString())
                }
            }
        })
    }

    fun setBtnEnable() {
        val input_cafeid = cafe_id
        val input_title = et_addreview_oneline.text.toString()//한줄 설명
        val input_content = et_addreview_multiline.text.toString() //상세 설명
        val input_rating = currentProgress// 별점
        var flag: Boolean = false
        Log.v(
            "postReviewWriteResponse",
            input_cafeid.toString() + "  " + input_rating.toString() + " " + input_title + " " + input_content + " " + images.toString()
        )
        img_addreview_complete.isEnabled =
                (input_title.isNotEmpty() && input_content.isNotEmpty() && input_cafeid != null && photoItems.size > 0)
        flag = true
        changeButtonColor(flag)
        Log.v("postReviewWriteResponse", "다 널 아니다")
        img_addreview_complete.setOnClickListener {
            postReviewWriteResponse(input_cafeid!!, input_content, input_rating, input_title)
//            finish()
        }
        if (!(input_title.isNotEmpty() && input_content.isNotEmpty() && input_cafeid != null && photoItems.size > 0)) {
            flag = false
            changeButtonColor(flag)
            img_addreview_complete.isEnabled = false
        }

    }

    fun getRealPathFromURI(content: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader: CursorLoader = CursorLoader(this, content, proj, null, null, null)
        val cursor: Cursor? = loader.loadInBackground()
        val column_idx = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor . moveToFirst ()
        val result = cursor.getString(column_idx)
        cursor.close()
        return result
    }
    }