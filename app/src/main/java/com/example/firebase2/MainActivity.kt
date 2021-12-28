package com.example.firebase2

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var auth:FirebaseAuth
    var REQUEST_CODE_SIGN_IN:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        btnGoogleSignIn.setOnClickListener {
            //below here we request to google about info we want
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build()
            val signInClient = GoogleSignIn.getClient(this, options)
            //the sign in client refers to that pop up that comes when we click the button to choose account
            signInClient.signInIntent.also {
                launchActivity.launch(it)
            }
        }
    }
    //the method below hre is used to get result from the activity we ran
    var launchActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if(result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result//here we receive the account selected by the user
            account?.let{
                //below here we Authenticate users gmail account to our FireBase
                googleFirebaseAuth(it)
            }
        }
    }
    //it is necessary to add SHA1 certificate to our firebase console ... we can access that from
    //Gradle(Top right Icon)->Tasks->signingReport->Copy SHA1 fingerprint->Add that fingerprint to firebase
    //console
    private fun googleFirebaseAuth(account:GoogleSignInAccount){
        //below we get the credentials from google about the gmail account
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()//we wait until the signin is done
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Successfully logged in", Toast.LENGTH_LONG).show()
                }
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}