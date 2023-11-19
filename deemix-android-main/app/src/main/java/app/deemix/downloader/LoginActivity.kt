package app.deemix.downloader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.deemix.downloader.SharedObjects.dz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        loginButton = findViewById<Button>(R.id.loginButton)
        emailField = findViewById<EditText>(R.id.emailField)
        passwordField = findViewById<EditText>(R.id.passwordField)
        loading = findViewById<ProgressBar>(R.id.loginLoading)

        loginButton.setOnClickListener {
            if (!emailField.text.isNullOrBlank() && !passwordField.text.isNullOrBlank()){
                GlobalScope.launch(Dispatchers.IO) {
                    toggleLogin(false)
                    val email = emailField.text.toString()
                    val password = passwordField.text.toString()

                    val accessToken = dz.getAccessToken(email, password)
                    if (accessToken == null) {
                        showToast("Couldn't retrieve accessToken")
                        toggleLogin(true)
                        return@launch
                    }
                    val arl = dz.getArlFromAccessToken(accessToken)
                    if (arl == null) {
                        showToast("Couldn't retrieve arl")
                        toggleLogin(true)
                        return@launch
                    }
                    val loggedIn = dz.login(arl)
                    if (loggedIn){
                        saveLogin(accessToken, arl)
                        val thisIntent = Intent(this@LoginActivity, MainActivity::class.java)
                        thisIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain"){
                            thisIntent.action = Intent.ACTION_SEND
                            thisIntent.type = "text/plain"
                            thisIntent.putExtra(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
                        }
                        this@LoginActivity.startActivity(thisIntent)
                        this@LoginActivity.finish()
                    } else {
                        showToast("Couldn't login")
                        toggleLogin(true)
                        return@launch
                    }
                }
            }
        }
        toggleLogin(true)
    }

    private fun saveLogin(accessToken: String, arl: String) {
        openFileOutput("login", Context.MODE_PRIVATE).use { output ->
            output.write("$accessToken\n$arl".toByteArray())
        }
    }

    private fun toggleLogin(value: Boolean){
        runOnUiThread {
            if (value) {
                loginButton.isEnabled = true
                loading.visibility = View.GONE
            } else {
                loginButton.isEnabled = false
                loading.visibility = View.VISIBLE
            }
        }
    }

    private fun showToast(text: String){
        runOnUiThread {
            Toast.makeText(
                this, text,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}