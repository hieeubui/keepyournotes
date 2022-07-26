package com.android.keepyournotes.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.android.keepyournotes.R
import com.android.keepyournotes.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        init()
        onEventHandle()

    }

    private fun init() {
        fAuth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://keepyournotes-d3dc6-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    }

    private fun onEventHandle() {

        binding.ivBack.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        binding.etName.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (binding.etName.text.toString().isEmpty()) {
                    binding.tvYourNameError.visibility = View.VISIBLE
                    binding.tvYourNameError.text = getText(R.string.please_enter_your_name)
                } else {
                    binding.tvYourNameError.visibility = View.GONE
                }
            } else {
                binding.tvYourNameError.visibility = View.GONE
            }
        }

        binding.etEmail.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (binding.etEmail.text.toString().isEmpty()) {
                    binding.tvEmailError.visibility = View.VISIBLE
                    binding.tvEmailError.text = getText(R.string.please_enter_your_email)
                } else {
                    if (isValidEmail(binding.etEmail.text.toString())) {
                        fAuth.fetchSignInMethodsForEmail(binding.etEmail.text.toString())
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    if (task.result.signInMethods.isNullOrEmpty()) {
                                        binding.tvEmailError.visibility = View.GONE
                                    } else {
                                        binding.tvEmailError.visibility = View.VISIBLE
                                        binding.tvEmailError.text = getText(R.string.email_exists)
                                    }
                                } else {
                                    Log.e("TAG", "fetchSignInMethodsForEmail: failed")
                                }
                            }
                    } else {
                        binding.tvEmailError.visibility = View.VISIBLE
                        binding.tvEmailError.text = getText(R.string.wrong_email_format)
                    }
                }
            } else {
                binding.tvEmailError.visibility = View.GONE
            }
        }

        binding.etPassword.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (binding.etPassword.text.toString().isEmpty()) {
                    binding.tvPasswordError.visibility = View.VISIBLE
                    binding.tvPasswordError.text = getText(R.string.please_enter_your_password)
                } else {
                    if (binding.etPassword.text.toString().length >= 8) {
                        binding.tvPasswordError.visibility = View.GONE
                    } else {
                        binding.tvPasswordError.visibility = View.VISIBLE
                        binding.tvPasswordError.text = getText(R.string.password_must_contain)
                    }
                }
            } else {
                binding.tvPasswordError.visibility = View.GONE
            }
        }

        binding.etRePassword.setOnFocusChangeListener { _, b ->
            if (!b) {
                if (binding.etPassword.text.toString() == binding.etRePassword.text.toString()) {
                    binding.tvRePasswordError.visibility = View.GONE
                } else {
                    binding.tvRePasswordError.visibility = View.VISIBLE
                    binding.tvRePasswordError.text = getText(R.string.password_not_match)
                }
            } else {
                binding.tvRePasswordError.visibility = View.GONE
            }
        }

        binding.btnRegister.setOnClickListener {
            binding.btnRegister.isEnabled = false
            if (binding.etName.text.toString().isNotEmpty() &&
                binding.etEmail.text.toString().isNotEmpty() &&
                binding.etPassword.text.toString().isNotEmpty() &&
                binding.etRePassword.text.toString().isNotEmpty() &&
                isValidEmail(binding.etEmail.text.toString()) &&
                binding.etPassword.text.toString() == binding.etRePassword.text.toString()
            ) {
                createUserProfile(binding.etName.text.toString(), binding.etEmail.text.toString())
                fAuth.createUserWithEmailAndPassword(
                    binding.etEmail.text.toString().trim(),
                    binding.etPassword.text.toString().trim()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("Firebase", "createUserWithEmail:success")
                            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show()
                            Intent(this, LoginActivity::class.java).also { intent ->
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Log.d("Firebase", "createUserWithEmail:failed")
                            Toast.makeText(
                                this,
                                getText(R.string.something_went_wrong).toString() + it.exception,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                binding.btnRegister.isEnabled = true
                Toast.makeText(this, getText(R.string.please_check_your_info), Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun createUserProfile(name: String, email: String) {
        val key = db.child("users").push().key.toString()
        val user = User(name.trim(), email.trim())
        db.child("users").child(key).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.w("TAG", "createUserProfile: success")
            } else {
                Log.e("TAG", "createUserProfile: ${it.exception}")
            }
        }
    }

    data class User(val name: String? = null, val email: String? = null)

    private fun isValidEmail(str: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

}