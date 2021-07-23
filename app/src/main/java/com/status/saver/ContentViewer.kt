package com.status.saver

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.status.saver.StatusFragment.Companion.isContent
import java.net.URLConnection

class ContentViewer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_viewer)
        val uri = intent.getParcelableExtra<Uri>("uri")
        val img = findViewById<ImageView>(R.id.imageView)
        val videoView = findViewById<VideoView>(R.id.videoView)
        if (isImageFile(uri?.path)) {
            Glide.with(this).load(uri).into(img)

        } else if (isVideoFile(uri?.path)) {
            img.visibility = View.GONE
            videoView.visibility = View.VISIBLE

            videoView.setVideoPath(uri?.path)
            videoView.setOnPreparedListener {
                videoView.start()
            }
            videoView.setOnCompletionListener {
                finish()
            }
        }
        isContent = true
    }

    private fun isImageFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("image")
    }

    private fun isVideoFile(path: String?): Boolean {
        val mimeType = URLConnection.guessContentTypeFromName(path)
        return mimeType != null && mimeType.startsWith("video")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        return
    }




}