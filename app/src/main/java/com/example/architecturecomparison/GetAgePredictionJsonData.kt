package com.example.architecturecomparison

import android.os.AsyncTask
import org.json.JSONException
import org.json.JSONObject

class GetAgePredictionJsonData(private val listener: OnAgePredictionAvailable):
    AsyncTask<String, Void, AgePrediction?>() {

    interface OnAgePredictionAvailable {
        fun onAgePredictionAvailable(data: AgePrediction)
        fun onError()
    }

    override fun doInBackground(vararg params: String): AgePrediction? {
        return try {
            val jsonParam = JSONObject(params[0])
            val name = jsonParam.getString("name")
            val age = jsonParam.getInt("age")

            AgePrediction(name, age)
        } catch (e: JSONException) {
            e.printStackTrace()
            cancel(true)
            null
        }
    }

    override fun onPostExecute(result: AgePrediction?) {
        super.onPostExecute(result)
        if (result != null) {
            listener.onAgePredictionAvailable(result)
        } else {
            listener.onError()
        }
    }
}
