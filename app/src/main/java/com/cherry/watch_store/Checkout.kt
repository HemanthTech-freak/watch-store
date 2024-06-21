package com.cherry.watch_store

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import java.util.*

class Checkout : AppCompatActivity() {

    //variable initialisation
    private lateinit var postalCodeEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var cardNumberEditText: EditText
    private lateinit var cardName: EditText
    private lateinit var expiryDateCvvLayout: LinearLayout
    private lateinit var expiryDateEditText: EditText
    private lateinit var cvvEditText: EditText

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var addressEditText: EditText

    private lateinit var user: FirebaseUser
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //get instance from the firebase for the products object
        database = FirebaseDatabase.getInstance().getReference("products")

        postalCodeEditText = findViewById(R.id.postalCodeCheckout)
        emailEditText = findViewById(R.id.emailCheckout)
        phoneEditText = findViewById(R.id.phoneCheckout)
        cardNumberEditText = findViewById(R.id.cardNumberCheckout)
        cardName = findViewById(R.id.cardNameCheckout)
        expiryDateCvvLayout = findViewById(R.id.expiryDateCvvLayout)
        expiryDateEditText = findViewById(R.id.expiryDateCheckout)
        cvvEditText = findViewById(R.id.cvvCheckout)

        firstNameEditText = findViewById(R.id.firstNameCheckout)
        lastNameEditText = findViewById(R.id.lastNameCheckout)
        addressEditText = findViewById(R.id.addressCheckout)

        // Set up validation and listeners for EditText fields
        setupValidation()
        setupPaymentOptionsListener()
        setupSubmitButton()
    }

    private fun setupValidation() {
        // Postal Code Validation
        postalCodeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length != 6) {
                    postalCodeEditText.error = "Postal code must be 6 characters"
                } else {
                    postalCodeEditText.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Email Validation
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = emailEditText.text.toString().trim()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.error = "Invalid email address"
                } else {
                    emailEditText.error = null
                }
            }
        }

        // Phone Number Validation
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length != 10) {
                    phoneEditText.error = "Phone number must be 10 digits"
                } else {
                    phoneEditText.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Card Number Validation and Masking
        cardNumberEditText.addTextChangedListener(object : TextWatcher {
            private val totalLength = 16
            private val partialMaskLength = 4
            private var deleting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length != 16) {
                    cardNumberEditText.error = "Card Number must be 16 digits"
                } else {
                    cardNumberEditText.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                cardNumberEditText.removeTextChangedListener(this)
                val currentText = s.toString()
                if (currentText.length < totalLength) {
                    cardNumberEditText.addTextChangedListener(this)
                    return
                }

                val masked = StringBuilder()
                for (i in 0 until totalLength) {
                    if (i < totalLength - partialMaskLength) {
                        masked.append('*')
                    } else {
                        masked.append(currentText[i])
                    }

                    if ((i + 1) % 4 == 0 && i < totalLength - 1) {
                        masked.append(' ')
                    }
                }

                cardNumberEditText.setText(masked.toString())
                cardNumberEditText.setSelection(masked.length)
                cardNumberEditText.addTextChangedListener(this)
            }
        })

        // Expiry Date Validation and Masking
        expiryDateEditText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var prevLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                prevLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                if (!isFormatting && length == 2 && prevLength < length) {
                    isFormatting = true
                    expiryDateEditText.setText("$s/")
                    expiryDateEditText.setSelection(expiryDateEditText.text.length)
                    isFormatting = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (!isFormatting) {
                    val expiryDate = s.toString().replace("/", "")
                    val expiry = isValidExpiryDate(expiryDate)
                    if (!expiry) {
                        expiryDateEditText.error = "Expiry Date must be a future Date"
                    } else {
                        expiryDateEditText.error = null
                    }
                }
            }
        })

        // CVV Validation
        cvvEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length != 3) {
                    cvvEditText.error = "CVV must be 3 digits"
                } else {
                    cvvEditText.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupPaymentOptionsListener() {
        // Payment Options Click Listener
        val paymentOptionsSpinner: Spinner = findViewById(R.id.paymentOptions)
        val paymentOptions = arrayOf("Select Payment Option", "Credit Card", "Debit Card")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        paymentOptionsSpinner.adapter = adapter
        paymentOptionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        cardNumberEditText.visibility = View.INVISIBLE
                        cardName.visibility = View.INVISIBLE
                        expiryDateCvvLayout.visibility = View.INVISIBLE
                    }
                    1, 2 -> { // Credit Card or Debit Card
                        cardNumberEditText.visibility = View.VISIBLE
                        cardName.visibility = View.VISIBLE
                        expiryDateCvvLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                cardNumberEditText.visibility = View.INVISIBLE
                cardName.visibility = View.INVISIBLE
                expiryDateCvvLayout.visibility = View.INVISIBLE
            }
        }
    }

    private fun hasError(): Boolean {
        var valid = true
        if (postalCodeEditText.error != null) valid = false
        if (emailEditText.error != null) valid = false
        if (phoneEditText.error != null) valid = false
        if (cardNumberEditText.error != null) valid = false
        if (expiryDateEditText.error != null) valid = false
        if (cvvEditText.error != null) valid = false
        return valid
    }

    //Submit button logic intent to products page
    private fun setupSubmitButton() {
        val submitButton: Button = findViewById(R.id.checkoutSubmit)
        submitButton.setOnClickListener {
            if (isValid() && hasError()) {
                startActivity(Intent(this, ConfirmationActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValid(): Boolean {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val postalCode = postalCodeEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val cardNumber = cardNumberEditText.text.toString().trim()
        val cardname = cardName.text.toString().trim()
        val expiryDate = expiryDateEditText.text.toString().trim()
        val cvv = cvvEditText.text.toString().trim()

        return !TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) &&
                !TextUtils.isEmpty(address) && !TextUtils.isEmpty(postalCode) &&
                !TextUtils.isEmpty(email) && !TextUtils.isEmpty(phone) &&
                !TextUtils.isEmpty(cardNumber) && !TextUtils.isEmpty(cardname) &&
                !TextUtils.isEmpty(expiryDate) && !TextUtils.isEmpty(cvv)
    }

    //Expiry date information validation
    private fun isValidExpiryDate(expiryDate: String): Boolean {
        if (!expiryDate.matches(Regex("\\d{4}"))) {
            return false
        }

        val month = expiryDate.substring(0, 2).toInt()
        val year = expiryDate.substring(2).toInt()

        if (month < 1 || month > 12) {
            return false
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        return if (year > currentYear || (year == currentYear && month >= currentMonth)) {
            true
        } else {
            false
        }
    }
}
