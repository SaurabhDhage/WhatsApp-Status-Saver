package com.status.saver


import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.status.saver.SavedFragment.Companion.flag
import java.io.File
import java.io.FileOutputStream


/**
 * A simple [Fragment] subclass.
 */
class StatusFragment : Fragment(), View.OnKeyListener {


    private var itemList = ArrayList<Model>()
    var recyclerViewMediaAdapter: ListAdapter? = null
    private lateinit var txt: TextView

    companion object {
        const val WHATSAPP_STATUSES_LOCATION = "/WhatsApp/Media/.Statuses"
        private lateinit var mRecyclerViewMediaList: RecyclerView
        var isSaved = false
        const val DIRECTORY_TO_SAVE_MEDIA_NOW = "/WhatsApp Status/"
        val selectedItems = ArrayList<Model>()
        var isContent = false
        var wpShare=false
        val savedItems = ArrayList<Model>()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            onSaveClick()
        }
        txt = view.findViewById(R.id.msg)
        mRecyclerViewMediaList = view.findViewById(R.id.recyclerViewMediaStatus)
       mRecyclerViewMediaList.isNestedScrollingEnabled=false

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(this)

        return view
    }

    override fun onStart() {
        super.onStart()
        if (!isContent && !wpShare) {
           createAdapter()
        }
        isContent = false
        wpShare=false
    }

    private fun createAdapter() {
        val mLinearLayoutManager = GridLayoutManager(context, 2)

        mRecyclerViewMediaList.layoutManager = mLinearLayoutManager
        refreshList()
        recyclerViewMediaAdapter = context?.let { ListAdapter(it, itemList) }
        mRecyclerViewMediaList.adapter = recyclerViewMediaAdapter
        if (itemList.isEmpty()) {
            txt.visibility = View.VISIBLE
            mRecyclerViewMediaList.visibility = View.GONE
        } else {
            txt.visibility = View.GONE
            mRecyclerViewMediaList.visibility = View.VISIBLE
        }


    }


    private fun refreshList() {
        itemList = getListFiles(
            File(
                Environment.getExternalStorageDirectory()
                    .toString() + WHATSAPP_STATUSES_LOCATION
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


    private fun onSaveClick() {

        if (selectedItems.isEmpty()) {
            Toast.makeText(context, "Please Select item using long press...", Toast.LENGTH_LONG)
                .show()
            return
        }

        val root = Environment.getExternalStorageDirectory()
        val folder = File(root.absolutePath + DIRECTORY_TO_SAVE_MEDIA_NOW)
        folder.mkdir()

        for (item in selectedItems) {
            val file = File(folder, item.file.name)
            val outputStream = FileOutputStream(file)
            outputStream.write(item.file.readBytes())

            outputStream.flush()
            outputStream.close()

        }

        Toast.makeText(context, "SAVED", Toast.LENGTH_SHORT).show()

        savedItems.addAll(selectedItems)
        flag = false
        isSaved = true

        justRefresh()

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (!isVisibleToUser && selectedItems.isNotEmpty()) {

            justRefresh()
        }
    }

    private fun justRefresh() {

        selectedItems.forEach {
            val index = itemList.indexOf(it)
            if (index == -1) return@forEach
            itemList[index].isSelected = false
            recyclerViewMediaAdapter?.notifyItemChanged(index)
        }
        selectedItems.clear()
        recyclerViewMediaAdapter?.mActionMode?.let { it.finish() }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                if (selectedItems.isEmpty())
                    activity?.onBackPressed()

                selectedItems.forEach {

                    val index = itemList.indexOf(it)
                    if (index == -1) return@forEach
                    itemList[index].isSelected = false
                 recyclerViewMediaAdapter?.notifyItemChanged(index)
                }
                selectedItems.clear()

                recyclerViewMediaAdapter?.mActionMode?.let { it.finish() }

                return true
            }
        }
        return false
    }
}