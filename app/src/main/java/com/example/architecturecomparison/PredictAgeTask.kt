package com.example.architecturecomparison

import android.os.AsyncTask
import java.net.URL

enum class AgePredictionStatus {
    OK, IDLE, NOT_INITIALIZED, ERROR
}
class GetRawAgePrediction(private val listener: OnAgePredictionComplete): AsyncTask<String, Void, String>() {

    private var agePredictionStatus = AgePredictionStatus.IDLE

    interface OnAgePredictionComplete {
        fun onAgePredictionComplete(data: String, status: AgePredictionStatus)
    }

    override fun doInBackground(vararg params: String?): String {
        if (params[0] == null) {
            agePredictionStatus = AgePredictionStatus.NOT_INITIALIZED
            return "No URL specified"
        }

        return try {
            agePredictionStatus = AgePredictionStatus.OK
            URL(params[0]).readText()
        } catch (e: Exception) {
            agePredictionStatus = AgePredictionStatus.ERROR
            "Error: ${e.message}"
        }
    }

    override fun onPostExecute(result: String) {
        listener.onAgePredictionComplete(result, agePredictionStatus)
    }
}