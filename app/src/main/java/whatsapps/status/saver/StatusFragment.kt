package whatsapps.status.saver

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import whatsapps.status.saver.SavedFragment.Companion.flag
import whatsapps.status.saver.SavedFragment.Companion.itemListSaved
import java.io.File
import java.io.FileOutputStream


/**
 * A simple [Fragment] subclass.
 */
class StatusFragment : Fragment(){


    private lateinit var itemList: ArrayList<Model>
    val WHATSAPP_STATUSES_LOCATION = "/WhatsApp/Media/.Statuses"
    var recyclerViewMediaAdapter: ListAdapter?=null
    companion object
    {
        private lateinit var txt: TextView
        private lateinit var mRecyclerViewMediaList: RecyclerView
        var isSaved=false
        const val DIRECTORY_TO_SAVE_MEDIA_NOW = "/WhatsApp Status/"
        val selectedItems=ArrayList<File>()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_status, container, false)

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
           onSaveClick()
        }
        txt=view.findViewById(R.id.msg)
        mRecyclerViewMediaList = view.findViewById(R.id.recyclerViewMediaStatus)

        createAdapter()
        return view
    }

    private fun createAdapter() {
        val mLinearLayoutManager = GridLayoutManager(context, 2)

        mRecyclerViewMediaList.layoutManager = mLinearLayoutManager
        itemList=getListFiles(
            File(
                Environment.getExternalStorageDirectory()
                    .toString() + WHATSAPP_STATUSES_LOCATION
            )
        )
        if(itemList.isEmpty())
        {
            txt.visibility=View.VISIBLE
            mRecyclerViewMediaList.visibility=View.GONE
        }
        else
        {
            txt.visibility=View.GONE
            mRecyclerViewMediaList.visibility=View.VISIBLE
            recyclerViewMediaAdapter = context?.let { ListAdapter(it, itemList) }
            mRecyclerViewMediaList.adapter = recyclerViewMediaAdapter
        }

    }

    private fun getListFiles(parentDir: File): ArrayList<Model> {
        val inFiles: ArrayList<Model> = ArrayList()
        val files: Array<File>? = parentDir.listFiles()
        if (files != null) {
            for (file in files) {
                Log.e("check", file.name)
                if (file.name.endsWith(".jpg") ||
                    file.name.endsWith(".gif") ||
                    file.name.endsWith(".mp4")) {
                    if (!inFiles.contains(file)) inFiles.add(Model(file))
                }
            }
        }
        return inFiles
    }

    private fun onSaveClick() {

        if(selectedItems.isEmpty())
        {
            Toast.makeText(context, "Please Select item using long press...", Toast.LENGTH_LONG).show()
            return
        }


        val root= Environment.getExternalStorageDirectory()
        val folder= File(root.absolutePath + DIRECTORY_TO_SAVE_MEDIA_NOW)
        folder.mkdir()
        for(item in selectedItems)
        {
            val file= File(folder, item.name)
            val outputStream= FileOutputStream(file)
            outputStream.write(item.readBytes())

            outputStream.flush()
            outputStream.close()

        }
        Toast.makeText(context, "SAVED", Toast.LENGTH_SHORT).show()

        selectedItems.clear()
        flag=false
        isSaved =true;
       createAdapter()

    }
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser  && selectedItems.isNotEmpty()) {

            selectedItems.clear()


           createAdapter()
        }
    }
}