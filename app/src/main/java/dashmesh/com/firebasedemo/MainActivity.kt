package dashmesh.com.firebasedemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import dashmesh.com.firebasedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    var scope = CoroutineScope(Dispatchers.Main)
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var GALLERY_REQUEST_CODE = 1001
    var listofproductImage : ArrayList<Uri> = ArrayList()
    var listofproductImageURL : ArrayList<String> = ArrayList()
    var mStorageReference = FirebaseStorage.getInstance().getReference();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
            selectImageFromGallery()
        }
    }

    private fun selectImageFromGallery() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            GALLERY_REQUEST_CODE
        )
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun authPhoneNumber(){

    }

    fun isAllImagesUploaded():MutableLiveData<Boolean>{
        val liveData = MutableLiveData<Boolean>()

        liveData.postValue(true)
        return liveData
    }

    private fun uploadImagesFirebase(){

        runBlocking {
            listofproductImageURL.clear()
            if (listofproductImage.size>0){
                for (pos in 0 until listofproductImage.size){
                    scope.async{
                        uploadImages(pos)
                    }
                }
            }
        }
    }

    suspend fun uploadImages(pos: Int){

        runBlocking {
            val fileToUpload: StorageReference = mStorageReference.child("Images").child("productname"+pos)
//                fileToUpload.putFile(listofproductImage.get(pos)).addOnSuccessListener { OnSuccessListener<UploadTask> {it->
//                    if (it.isSuccessful){
//                        Log.d("ImageUrl : ",it.result.uploadSessionUri!!.path.toString())
//                    }
//                } }


            fileToUpload.putFile(listofproductImage[pos])
                .addOnSuccessListener(this@MainActivity,  OnSuccessListener<UploadTask.TaskSnapshot>() {

                    val content = it.uploadSessionUri!!.path.toString()
                    listofproductImageURL.add(content)
                    Log.d("ImageUrl",content)
                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener {

                }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            listofproductImage = ArrayList()
            //data.clipData.getItemAt(0).uri

            try {
                var logo_file_uri = data.data!!
                listofproductImage.add(logo_file_uri)
            }catch (e:Exception){
                Log.v("CompanyDetails",e.message.toString())
            }
            uploadImagesFirebase()
            Toast.makeText(this@MainActivity , "complete",Toast.LENGTH_LONG).show()
        }else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null){

            try {
                listofproductImage = ArrayList()
                if (data.clipData!=null){
                    if (data.clipData!!.itemCount>0){
                        for (pos in 0 until data.clipData!!.itemCount){
                            listofproductImage.add(data.clipData!!.getItemAt(pos).uri)
                        }
                    }
                }

            }catch (e:Exception){
                Log.v("CompanyDetails",e.message.toString())
            }
            uploadImagesFirebase()
            Toast.makeText(this@MainActivity , "complete",Toast.LENGTH_LONG).show()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}