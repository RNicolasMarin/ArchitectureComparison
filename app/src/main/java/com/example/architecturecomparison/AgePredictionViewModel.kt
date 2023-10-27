package com.example.architecturecomparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.architecturecomparison.DataState.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgePredictionViewModel: ViewModel() {

    private var service = RetrofitHelper.getInstance().create(AgePredictionApi::class.java)

    var currentText = MutableStateFlow("")
    var predictionState = MutableStateFlow<DataState<AgePrediction>>(Idle)

    fun getPrediction() {
        predictionState.value = Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = service.getAgePrediction(currentText.value)
            withContext(Dispatchers.Main) {
                if (result.isSuccessful) {
                    val agePrediction = result.body()
                    if (agePrediction == null) {
                        predictionState.value = Error
                    } else {
                        predictionState.value = Success(agePrediction)
                    }
                } else {
                    predictionState.value = Error
                }
            }
        }
    }
}