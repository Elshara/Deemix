package app.deemix.downloader

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceFragmentCompat
import app.deemix.downloader.SharedObjects.dz
import app.deemix.downloader.types.DeezerQuality
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class SettingsActivity : AppCompatActivity() {

    private lateinit var loggedInPanel:ConstraintLayout
    private lateinit var userAvatar:ImageView
    private lateinit var userName:TextView
    private lateinit var userInfo:TextView
    private lateinit var logoutButton:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preferencesFragment, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loggedInPanel = findViewById<ConstraintLayout>(R.id.loggedInSection)
        userAvatar = findViewById<ImageView>(R.id.userAvatar)
        userName = findViewById<TextView>(R.id.userName)
        userInfo = findViewById<TextView>(R.id.userInfo)
        logoutButton = findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                dz.logout()
                deleteFile("login")
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this@SettingsActivity.startActivity(intent)
                this@SettingsActivity.finish()
            }
        }

        updateLoginPanels()
    }

    fun getDisplaySubscription(canStream: DeezerQuality): String{
        if (canStream.lossless) return "Hi-Fi"
        if (canStream.high) return "Premium"
        return "Free"
    }

    fun updateLoginPanels(){
        if (dz.isLoggedIn){
            userName.text = dz.currentUser!!.name
            userInfo.text = "${getDisplaySubscription(dz.currentUser!!.canStream)} | ${dz.currentUser!!.country}"
            Picasso.get().load("https://e-cdns-images.dzcdn.net/images/user/${dz.currentUser!!.picture}/125x125-000000-80-0-0.jpg")
                .into(userAvatar)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }
    }
}