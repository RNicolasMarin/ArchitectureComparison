package com.example.architecturecomparison

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

private const val EDIT_TEXT_NAME_VALUE = "EDIT_TEXT_NAME_VALUE"
private const val TEXT_VIEW_PREVIOUS_NAME = "TEXT_VIEW_PREVIOUS_NAME"
private const val TEXT_VIEW_PREVIOUS_AGE = "TEXT_VIEW_PREVIOUS_AGE"

class MainActivityOld : AppCompatActivity(), GetRawAgePrediction.OnAgePredictionComplete,
    GetAgePredictionJsonData.OnAgePredictionAvailable {

    private var etName: EditText? = null
    private var btPredictAge: Button? = null
    private var pbLoading: ProgressBar? = null
    private var tvError: TextView? = null
    private var tvAgePredictionName: TextView? = null
    private var tvAgePredictionAge: TextView? = null
    private var clResult: ConstraintLayout? = null
    private var name: String = ""
    private var age: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        val btPredictAge: Button = findViewById(R.id.btPredictAge)
        pbLoading = findViewById(R.id.pbLoading)
        clResult = findViewById(R.id.clResult)
        tvAgePredictionName = findViewById(R.id.tvAgePredictionName)
        tvAgePredictionAge = findViewById(R.id.tvAgePredictionAge)
        tvError = findViewById(R.id.tvError)

        etName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                btPredictAge.isEnabled = etName?.text.toString().isNotBlank()
            }
        })

        btPredictAge.setOnClickListener { requestAge() }
    }

    private fun requestAge() {
        closeKeyboard()
        pbLoading?.visibility = View.VISIBLE
        btPredictAge?.isEnabled = false
        etName?.isEnabled = false

        val url = "https://api.agify.io/?name=${etName?.text.toString().trim()}"
        val getPrediction = GetRawAgePrediction(this)
        getPrediction.execute(url)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        etName?.setText(savedInstanceState.getString(EDIT_TEXT_NAME_VALUE, ""))
        name = savedInstanceState.getString(TEXT_VIEW_PREVIOUS_NAME, "")
        age = savedInstanceState.getInt(TEXT_VIEW_PREVIOUS_AGE, -1)
        btPredictAge?.isEnabled = etName?.text.toString().isNotBlank()
        tvAgePredictionName?.text = name
        tvAgePredictionAge?.text = age.toString()
        clResult?.visibility = if (name.isNotBlank()) View.VISIBLE else View.INVISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDIT_TEXT_NAME_VALUE, etName?.text.toString())
        outState.putString(TEXT_VIEW_PREVIOUS_NAME, name)
        outState.putInt(TEXT_VIEW_PREVIOUS_AGE, age)
    }

    private fun closeKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus

        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }

    override fun onAgePredictionComplete(data: String, status: AgePredictionStatus) {
        if (status == AgePredictionStatus.OK) {
            val getAgePredictionJsonData = GetAgePredictionJsonData(this)
            getAgePredictionJsonData.execute(data)
        } else {
            onError()
        }
    }

    override fun onAgePredictionAvailable(data: AgePrediction) {
        pbLoading?.visibility = View.GONE
        name = data.name
        age = data.age
        tvAgePredictionName?.text = name
        tvAgePredictionAge?.text = age.toString()
        clResult?.visibility = View.VISIBLE
        btPredictAge?.isEnabled = true
        etName?.isEnabled = true
    }

    override fun onError() {
        pbLoading?.visibility = View.GONE
        tvError?.visibility = View.VISIBLE
        btPredictAge?.isEnabled = true
        etName?.isEnabled = true
    }

}