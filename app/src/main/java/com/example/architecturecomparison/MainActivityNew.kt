package com.example.architecturecomparison

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.architecturecomparison.DataState.*
import com.example.architecturecomparison.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivityNew : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AgePredictionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() = with(binding) {
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.currentText.value = s.toString()
            }
        })

        btPredictAge.setOnClickListener { requestAge() }

        collectLatestLifeCycleFlow(viewModel.currentText) { text ->
            btPredictAge.isEnabled = text.isNotBlank()
        }
        collectLatestLifeCycleFlow(viewModel.predictionState) { state ->
            pbLoading.visibleOrGone(state is Loading)
            //btPredictAge.isEnabled = state !is Loading
            etName.isEnabled = state !is Loading
            if (state is Success) {
                tvAgePredictionName.text = state.data.name
                tvAgePredictionAge.text = state.data.age.toString()
            }
            clResult.visibleOrInvisible(state is Success)
            tvError.visibleOrGone(state is Error)
        }
    }

    private fun requestAge() {
        closeKeyboard()
        viewModel.getPrediction()
    }

    private fun closeKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus

        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }
}

fun <T> AppCompatActivity.collectLatestLifeCycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

fun View.visibleOrGone(isVisible: Boolean) {
    visibility = if (isVisible) VISIBLE else GONE
}

fun View.visibleOrInvisible(isVisible: Boolean) {
    visibility = if (isVisible) VISIBLE else INVISIBLE
}