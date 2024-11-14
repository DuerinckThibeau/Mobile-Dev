package com.example.rentapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        setupCategorySpinner()
        setupConfirmButton()
    }

    private fun setupCategorySpinner() {
        val spinner: Spinner = findViewById(R.id.categorySpinner)
        
        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            resources.getStringArray(R.array.categories_array)
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#80FFFFFF"))
                } else {
                    view.setTextColor(Color.WHITE)
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.parseColor("#80FFFFFF"))
                } else {
                    view.setTextColor(Color.WHITE)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                view?.let {
                    (it as TextView).setTextColor(
                        if (position == 0) Color.parseColor("#80FFFFFF")
                        else Color.WHITE
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupConfirmButton() {
        findViewById<Button>(R.id.confirmButton).setOnClickListener {
            val spinner: Spinner = findViewById(R.id.categorySpinner)
            
            if (spinner.selectedItem.toString() == "Category") {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }
    }
} 