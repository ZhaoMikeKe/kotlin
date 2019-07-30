package com.huachu.myapplication

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
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
import android.os.Environment.getExternalStorageDirectory
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.ToastUtils
import com.huachu.myapplication.R.id.imageView
import com.huachu.myapplication.R.id.textView
import com.huachu.myapplication.service.FloatingButtonService
import com.huachu.myapplication.service.FloatingVideoService
import com.huachu.mylibrary.FileProvider7
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.tbruyelle.rxpermissions2.RxPermissions


class MainActivity : AppCompatActivity() {
    val REQUEST_CODE_TAKE_PHOTO = 0
    var mCurrentPhotoPath = ""
    public lateinit var floatingButton: Button
    public lateinit var layoutParams: WindowManager.LayoutParams
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        OkGo.getInstance().init(application)
        //getData()
        val rxPermission = RxPermissions(this)
        val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        takePhoto.setOnClickListener(View.OnClickListener {
            rxPermission
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    )
                    .subscribe { granted ->
                        if (granted!!) {
                            // All requested permissions are granted
                            takePhotoNoCompress()
                        } else {
                            // At least one permission is denied

                        }
                    }

        })
        clearData.setOnClickListener(View.OnClickListener {
            am.clearApplicationUserData()
        })
        getData.setOnClickListener(View.OnClickListener {
            getDataRetrofit()
        })
        getAppTasks.setOnClickListener(View.OnClickListener {
            //commonROMPermissionCheck(MainActivity.this)
            if (commonROMPermissionCheck(this)) {
                addWindow()
            } else {
                requestAlertWindowPermission()
                ToastUtils.showShort("请授予悬浮窗权限")
            }


        })
    }


    private fun addWindow() {
        startService(Intent(this@MainActivity, FloatingVideoService::class.java))
        /* floatingButton = Button(this)
         floatingButton.text = "button"
         layoutParams = WindowManager.LayoutParams(
                 WindowManager.LayoutParams.WRAP_CONTENT,
                 WindowManager.LayoutParams.WRAP_CONTENT,
                 0, 0,
                 PixelFormat.TRANSPARENT
         )
         // flag 设置 Window 属性
         layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
         // type 设置 Window 类别（层级）
         layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
         layoutParams.gravity = Gravity.CENTER
         val windowManager = windowManager
         floatingButton.setOnTouchListener(FloatingOnTouchListener())
         windowManager.addView(floatingButton, layoutParams)*/
    }


    private val REQUEST_CODE = 1

    //判断权限
    private fun commonROMPermissionCheck(context: Activity): Boolean {
        var result: Boolean? = true
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                val clazz = Settings::class.java
                val canDrawOverlays = clazz!!.getDeclaredMethod("canDrawOverlays", Context::class.java)
                result = canDrawOverlays.invoke(null, context) as Boolean?
            } catch (e: Exception) {

            }

        }
        return result!!
    }

    //申请权限
    private fun requestAlertWindowPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, REQUEST_CODE)
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
            return
        }
        if (requestCode == REQUEST_CODE) {
            addWindow()
            return
        }
        // else tip?

    }

    /* var mLastX: Float = 0.0F
     var mLastY: Float = 0.0F
     override fun onTouchEvent(event: MotionEvent?): Boolean {
         val mInScreenX = event!!.rawX
         val mInScreenY = event.rawY
         when (event.action) {
             MotionEvent.ACTION_DOWN -> {
                 mLastX = event.rawX
                 mLastY = event.rawY

             }
             MotionEvent.ACTION_MOVE -> {
                 layoutParams.x = layoutParams.x.plus(mInScreenX - mLastX).toInt()
                 layoutParams.y = layoutParams.y.plus(mInScreenY - mLastY).toInt()
                 mLastX = mInScreenX
                 mLastY = mInScreenY
                 windowManager.updateViewLayout(floatingButton, layoutParams)

             }
         }
         return super.onTouchEvent(event)
     }*/

}





