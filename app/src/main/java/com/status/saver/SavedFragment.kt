package com.status.saver

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.status.saver.StatusFragment.Companion.DIRECTORY_TO_SAVE_MEDIA_NOW
import com.status.saver.StatusFragment.Companion.isSaved
import com.status.saver.StatusFragment.Companion.savedItems
import com.status.saver.StatusFragment.Companion.selectedItems
import java.io.File


class SavedFragment : Fragment(), View.OnClickListener, View.OnKeyListener {
    companion object {
        var flag = false

        var itemListSaved = ArrayList<Model>()
    }

    var mRecyclerViewMediaAdapter: ListAdapter? = null
    private lateinit var txt: TextView
    private lateinit var mRecyclerViewMediaList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved, container, false)
        txt = view.findViewById(R.id.msgSaved)
        mRecyclerViewMediaList = view.findViewById(R.id.recyclerViewMediaSaved)
        mRecyclerViewMediaList.isNestedScrollingEnabled = false
        view.findViewById<FloatingActionButton>(R.id.share).setOnClickListener {
            onShareClick()
        }
        view.findViewById<FloatingActionButton>(R.id.delete).setOnClickListener(this)
        createAdapter()
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(this)

        return view
    }

    private fun onShareClick() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(context, "Please Select item using long press...", Toast.LENGTH_LONG)
                .show()
            return
        }
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        val list = ArrayList<Uri>()
        selectedItems.forEach { k ->
            val uri = context?.let {
                FileProvider.getUriForFile(
                    it,
                    requireContext().applicationContext.packageName + ".provider",
                    k.file
                )
            }
            if (uri != null) {
                list.add(uri)
            }
        }
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list)

        shareIntent.type = "*/*"

        val extraMimeTypes = arrayOf("video/*", "image/*")
        shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
        shareIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {

            startActivityForResult(Intent.createChooser(shareIntent, "Share Via..."), 0)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Something Went Wrong..", Toast.LENGTH_LONG).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            selectedItems.clear()
            itemListSaved.forEach {
                if (it.isSelected) {
                    it.isSelected = false
                    mRecyclerViewMediaAdapter?.notifyItemChanged(itemListSaved.indexOf(it))
                    mRecyclerViewMediaAdapter?.mActionMode?.let { it.finish() }
                }

            }

        }


    }

    private fun createAdapter() {
        val mLinearLayoutManager = GridLayoutManager(context, 2)
        mRecyclerViewMediaList.layoutManager = mLinearLayoutManager
        refreshList()
        mRecyclerViewMediaAdapter =
            context?.let { ListAdapter(it, itemListSaved) }
        mRecyclerViewMediaList.adapter = mRecyclerViewMediaAdapter
        visibilityControl()

    }

    private fun visibilityControl() {
        if (itemListSaved.isEmpty()) {
            mRecyclerViewMediaList.visibility = View.GONE
            txt.visibility = View.VISIBLE
        } else {
            mRecyclerViewMediaList.visibility = View.VISIBLE
            txt.visibility = View.GONE

        }
    }

    private fun refreshList() {
        itemListSaved = getListFiles(
            File(
                Environment.getExternalStorageDirectory()
                    .toString() + DIRECTORY_TO_SAVE_MEDIA_NOW
            )
        )
    }

    private fun getListFiles(parentDir: File): ArrayList<Model> {
        val inFiles: ArrayList<Model> = ArrayList()
        val files: Array<File>? = parentDir.listFiles()
        if (files != null) {
            for (file in files) {
                Log.e("check", file.name)
                if (file.name.endsWith(".jpg") ||
                    file.name.endsWith(".gif") ||
                    file.name.endsWith(".mp4")
                ) {
                    if (!inFiles.contains(file)) inFiles.add(Model(file))
                }
            }
        }
        return inFiles
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser && isSaved) {

            savedItems.forEach {
                if (!itemListSaved.contains(it)) {
                    val index = itemListSaved.size
                    itemListSaved.add(it)
                    mRecyclerViewMediaAdapter?.notifyItemInserted(index)
                }
            }

            visibilityControl()
            savedItems.clear()

            isSaved = false
        } else if (!isVisibleToUser && selectedItems.isNotEmpty()) {

            justRefresh()


        }
    }

    private fun justRefresh() {

        selectedItems.forEach {

            val index = itemListSaved.indexOf(it)
            if (index == -1) return@forEach
            itemListSaved[index].isSelected = false
            mRecyclerViewMediaAdapter?.notifyItemChanged(index)
        }
        selectedItems.clear()
        mRecyclerViewMediaAdapter?.mActionMode?.finish()

    }

    override fun onClick(v: View) {
        if (selectedItems.isEmpty()) {
            Toast.makeText(context, "Please Select item using long press...", Toast.LENGTH_LONG)
                .show()
            return
        }


        val root = Environment.getExternalStorageDirectory()
        val folder = File(root.absolutePath + DIRECTORY_TO_SAVE_MEDIA_NOW)
        selectedItems.forEach {
            val index = itemListSaved.indexOf(it)
            mRecyclerViewMediaAdapter?.notifyItemRemoved(index)
            itemListSaved[index].isSelected=false
            itemListSaved.removeAt(index)
            val file = File(folder, it.file.name)
            file.delete()
        }
        selectedItems.clear()
        mRecyclerViewMediaAdapter?.mActionMode?.finish()
        if (itemListSaved.isEmpty()) {
            mRecyclerViewMediaAdapter?.notifyDataSetChanged()
            visibilityControl()
        }


    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {

        if (event != null) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                if (selectedItems.isEmpty())
                    activity?.onBackPressed()

                selectedItems.forEach {

                    val index = itemListSaved.indexOf(it)
                    if (index == -1) return@forEach
                    itemListSaved[index].isSelected = false
                    mRecyclerViewMediaAdapter?.notifyItemChanged(index)
                }
                selectedItems.clear()
                mRecyclerViewMediaAdapter?.mActionMode?.finish()

                return true
            }
        }
       return false


    }


}



