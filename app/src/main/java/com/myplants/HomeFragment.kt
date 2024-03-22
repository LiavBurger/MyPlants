package com.myplants

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class HomeFragment : Fragment() {

    private val client = OkHttpClient()
    private val apiKey = "tcOujrD7D3cV0pWTK0eb7QeF"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnMyProfile = view.findViewById<Button>(R.id.btnMyProfile)
        btnMyProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        fetchRandomFact(view)
    }

    private fun fetchRandomFact(view: View) {
        val factTextView = view.findViewById<TextView>(R.id.random_plant_fact_tv)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("https://api.fungenerators.com/fact/random?category=plants")
                    .addHeader("X-Fungenerators-Api-Secret", apiKey)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    print(responseBody)
                    val json = JSONObject(responseBody)
                    val factObject = json.getJSONObject("contents")
                    val fact = factObject.getString("fact")

                    withContext(Dispatchers.Main) {
                        factTextView.text = fact
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If there is an error, set the default fact
                val fact = "Photosynthesis is the process by which plants make their food."
                withContext(Dispatchers.Main) {
                    factTextView.text = fact
                }
            }
            finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}