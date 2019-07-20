package com.huachu.myapplication

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.huachu.myapplication.bean.Result
import com.huachu.myapplication.bean.ResultBean
import com.huachu.myapplication.network.ApiSource
import com.huachu.myapplication.network.Repository

import com.huachu.myapplication.network.Repository.adapterCoroutineQuery
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import android.provider.MediaStore
import com.huachu.mylibrary.FileProvider7
import android.os.Environment.getExternalStorageDirectory
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.view.View
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    val REQUEST_CODE_TAKE_PHOTO = 0
    var mCurrentPhotoPath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OkGo.getInstance().init(application)
        //getData()

        getDataRetrofit()


        takePhoto.setOnClickListener(View.OnClickListener {
            takePhotoNoCompress()
        })
    }


    private fun getDataRetrofit() {
        presenterScope.launch {
            val time = System.currentTimeMillis()
            try {
                val ganks = adapterCoroutineQuery()
                Log.d("zhaooo", ganks.toString())

                withContext(Dispatchers.Main) {
                    textView.text = ganks[0].who
                }
            } catch (e: Throwable) {

            } finally {
                Log.d("zhaooo", "耗时：${System.currentTimeMillis() - time}")
            }
        }
    }

    private val presenterScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.Main + Job())
    }


    private fun getData() {
        OkGo.get<String>("http://gank.io/api/data/Android/2/1")
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        val aa = response.body()
                        val bean = Gson().fromJson(aa, ResultBean::class.javaObjectType)
                        val results = bean.results
                        //验证是否有数据
                        val s = results.toString()
                        Log.e("main", s)
                    }

                    override fun onError(response: Response<String>) {
                        super.onError(response)
                        val aa = response.message()

                    }
                })

    }

    fun takePhotoNoCompress() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val filename = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA)
                    .format(Date()) + ".png"
            val file = File(getExternalStorageDirectory().path + "/OriPicture", filename)
            //val file = createOriImageFile()
            mCurrentPhotoPath = file.getAbsolutePath()
            val fileUri = FileProvider7.getUriForFile(this, file)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO)
        }
    }

    //原图像 路径
    private var imgPathOri: String? = null

    @Throws(IOException::class)
    private fun createOriImageFile(): File {
        val imgNameOri = "HomePic_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val pictureDirOri = File(getExternalStorageDirectory().path + "/OriPicture")
        if (!pictureDirOri.exists()) {
            pictureDirOri.mkdirs()
        }
        val image = File.createTempFile(
                imgNameOri,
                ".jpg",
                pictureDirOri
        )
        imgPathOri = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_TAKE_PHOTO) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath))
        }
        // else tip?

    }

}



