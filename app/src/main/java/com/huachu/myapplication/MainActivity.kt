package com.huachu.myapplication

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.huachu.myapplication.bean.ResultBean

import com.huachu.myapplication.network.Repository.adapterCoroutineQuery
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import android.provider.MediaStore
import android.os.Environment.getExternalStorageDirectory
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.tabs.TabLayoutMediator
import com.huachu.myapplication.activity.TablayoutActivity
import com.huachu.myapplication.adapter.DemoViewPagerAdapter
import com.huachu.myapplication.service.FloatingVideoService
import com.huachu.mylibrary.FileProvider7
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.tbruyelle.rxpermissions2.RxPermissions
import java.io.FileNotFoundException
import java.io.FileOutputStream


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
        getMemory.setOnClickListener {
            ToastUtils.showShort(am.memoryClass.toString())
        }
        setViewPager()
        gettablayout.setOnClickListener {
            val myIntent = Intent(this, TablayoutActivity::class.java)
            startActivity(myIntent)
        }

        map.setOnClickListener {
            flatmapAndmap()
        }
        fenxiang.setOnClickListener {
            share()
        }
        fenxiangimg.setOnClickListener {
            shareimg()
        }
    }

    private fun shareimg() {

        var bgimg0 = ImageUtils.getBitmap(R.mipmap.ic_launcher);
        var share_intent = Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("image/*");  //设置分享内容的类型
        share_intent.putExtra(Intent.EXTRA_STREAM, saveBitmap(bgimg0, "img"));
        //创建分享的Dialog
        share_intent = Intent.createChooser(share_intent, "分享");
        startActivity(share_intent);
    }

    /** * 将图片存到本地  */
    private fun saveBitmap(bm: Bitmap, picName: String): Uri? {
        try {
            val dir = Environment.getExternalStorageDirectory().absolutePath + "/renji/" + picName + ".jpg"
            val f = File(dir)
            if (!f.exists()) {
                f.parentFile.mkdirs()
                f.createNewFile()
            }
            val out = FileOutputStream(f)
            bm.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
            return Uri.fromFile(f)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun share() {

        var shareintent = Intent()
        shareintent.setAction(Intent.ACTION_SEND)//设置分享行为
        shareintent.setType("text/plain")//设置分享内容的类型
        shareintent.putExtra(Intent.EXTRA_SUBJECT, "标题")//添加分享内容标题
        shareintent.putExtra(Intent.EXTRA_TEXT, "www.baidu.com")//添加分享内容
//创建分享的Dialog
        shareintent = Intent.createChooser(shareintent, "分享")
        startActivity(shareintent)

    }

    private fun flatmapAndmap() {

        val suits = setOf("♣" /* clubs*/, "♦" /* diamonds*/, "♥" /* hearts*/, "♠" /*spades*/)
        val values = setOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")
        val deck = suits.flatMap { suit ->
            values.map { value -> LogUtils.d("zhaooo", suit.plus(value)) }
        }
        ToastUtils.showShort("请查看log")

    }

    private fun setViewPager() {

        viewpager.adapter = DemoViewPagerAdapter()
        TabLayoutMediator(tabLayout, viewpager, TabLayoutMediator.OnConfigureTabCallback { tab, position ->
            // Styling each tab here
            tab.text = "Tab $position"
        }).attach()
        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
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
    class EvenItemDecoration(private val space: Int, private val column: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
// 每个span分配的间隔大小
            val spanSpace = space * (column + 1) / column
// 列索引
            val colIndex = position % column
// 列左、右间隙
            outRect.left = space * (colIndex + 1) - spanSpace * colIndex
            outRect.right = spanSpace * (colIndex + 1) - space * (colIndex + 1)
// 行间距
            if (position >= column) {
                outRect.top = space
            }
        }
    }
}





