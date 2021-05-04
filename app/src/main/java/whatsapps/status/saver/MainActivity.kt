package whatsapps.status.saver


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.elevation = 0f;
        setContentView(R.layout.tab_activity)





        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        viewPager.offscreenPageLimit = 2

        //Initializing the tablayout
        val tabLayout = findViewById<View>(R.id.tabLayout) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {


            override fun onPageSelected(position: Int) {
                viewPager.setCurrentItem(position, false)

            }

           
        })


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED))
        {
            setupViewPager()
        } else {

            requestStoragePermission()
        }


    }

    private fun setupViewPager() {

            val adapter = ViewPagerAdapter(supportFragmentManager)
            val songsFragment = StatusFragment()
            val chatFragment = SavedFragment()

            adapter.addFragment(songsFragment, "STATUS")
            adapter.addFragment(chatFragment, "SAVED")

            viewPager.adapter = adapter

    }

    private lateinit var viewPager: ViewPager
    private val STORAGE_PERMISSON_CODE = 1

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )) {
            AlertDialog.Builder(this).setTitle("Permission Needed").setMessage("This permission needed to save status to your device")
                    .setPositiveButton("ok") { _, _ ->
                        ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                STORAGE_PERMISSON_CODE
                        )
                    }
                    .setNegativeButton("cancel") { dialog, which ->
                        dialog.dismiss()
                    }.create().show()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSON_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSON_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupViewPager()
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
                requestStoragePermission()
            }
        }

    }
}


