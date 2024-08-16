package com.vimalcvs.trello.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vimalcvs.trello.databinding.ActivityIntroBinding
import com.vimalcvs.trello.firebase.FireStoreClass

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUserId = FireStoreClass().getCurrentUserId()
        if (currentUserId.isNotBlank() || currentUserId.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.introBtnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.introBtnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}