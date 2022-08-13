package com.example.privatealbum.home.browser

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.privatealbum.R
import com.example.privatealbum.databinding.FragmentBrowserBinding
import com.example.privatealbum.home.thumb.ThumbViewModel
import kotlinx.coroutines.*
import java.util.*
import kotlin.properties.Delegates

class BrowserFragment : Fragment() {
    lateinit var binding: FragmentBrowserBinding
    private val model:ThumbViewModel by activityViewModels()
    private val args:BrowserFragmentArgs by navArgs()
    private lateinit var browserAdapter: BrowserAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var job: Job
    private var currentIndex = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            binding.pageTextView.text = "${currentIndex+1}/${model.thumbImageList.value!!.size}"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrowserBinding.inflate(inflater)

        model.thumbImageList.observe(viewLifecycleOwner){
            browserAdapter.setData(it)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentIndex = args.currentIndex

        browserAdapter = BrowserAdapter(model)
        val snapHelper = PagerSnapHelper()
        linearLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.browserRecyclerView.apply {
            adapter = browserAdapter
            layoutManager = linearLayoutManager
            snapHelper.attachToRecyclerView(this)
            setHasFixedSize(true)
        }

       //进入界面就立刻滚动到index对应的位置
        linearLayoutManager.scrollToPosition(currentIndex)

        binding.browserRecyclerView.addOnScrollListener(object:RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    //获取当前是第几页
                    currentIndex = linearLayoutManager.findFirstVisibleItemPosition()
                }
            }
        })

        //幻灯片播放按钮事件
        binding.playBtn.setOnClickListener {
            val tag = binding.playBtn.tag as String
            if(tag == "0") {
                startPlaySlider()
                binding.playBtn.tag = "1"
            }else{
                stopSlider()
                binding.playBtn.tag = "0"
            }
        }

        binding.deleteFileBtn.setOnClickListener {
            model.deleteImage(model.thumbImageList.value!![currentIndex])
            if (currentIndex > 0){
                if (model.thumbImageList.value!!.isNotEmpty()){
                    currentIndex--
                    linearLayoutManager.scrollToPosition(currentIndex)
                }else{
                    findNavController().navigateUp()
                }
            }
        }

        binding.shareBtn.setOnClickListener {
            shareImage()
        }

        //给容器添加点击事件
        binding.root.setOnClickListener {
            toggleBarView(false)
        }
    }
    fun startPlaySlider(){
        binding.playBtn.setImageResource(R.drawable.ic_pause)
        job = lifecycleScope.launch(Dispatchers.IO){
            while (true){
                delay(1000)
                withContext(Dispatchers.Main){
                    if (currentIndex + 1 >= model.thumbImageList.value!!.size){
                        currentIndex = 0
                        linearLayoutManager.scrollToPosition(currentIndex)
                    }else{
                        currentIndex ++
                        linearLayoutManager.smoothScrollToPosition(
                            binding.browserRecyclerView,
                            RecyclerView.State(),
                            currentIndex
                        )
                    }
                }
            }
        }
    }
    fun stopSlider(){
        binding.playBtn.setImageResource(R.drawable.ic_play)
        lifecycleScope.launch {
            job.cancelAndJoin()
        }
    }

    fun toggleBarView(show:Boolean){
        Log.v("pxd","toggleBar View")
    }

    fun shareImage(){
        val currentThumbImage = model.thumbImageList.value!![currentIndex]
        val filePath = model.getOriginImageFilePath(currentThumbImage.imageName)
        //val uri = Uri.parse(filePath)

        val mediauri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }else{
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpg")
            put(MediaStore.Images.Media.DISPLAY_NAME, "123")
        }

        val uri =  requireActivity().contentResolver.insert(mediauri,contentValues)
        uri?.let {
            requireActivity().contentResolver.openOutputStream(uri).use { os ->
                val bitmap = BitmapFactory.decodeFile(filePath)
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,os)
            }
        }

        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM,uri)
            type = "image/*"
            startActivity(this)
        }
    }
}









