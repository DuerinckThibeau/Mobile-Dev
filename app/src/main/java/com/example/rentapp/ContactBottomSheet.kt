package com.example.rentapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ContactBottomSheet : BottomSheetDialogFragment() {
    private lateinit var ownerNameText: TextView
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_contact, container, false)
        
        ownerNameText = view.findViewById(R.id.ownerNameText)
        startDateInput = view.findViewById(R.id.startDateInput)
        endDateInput = view.findViewById(R.id.endDateInput)
        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)

        arguments?.getString("ownerName")?.let {
            ownerNameText.text = it
        }

        setupDatePickers()
        setupSendButton()

        return view
    }

    private fun setupDatePickers() {
        startDateInput.setOnClickListener {
            showDatePicker(startDateInput)
        }

        endDateInput.setOnClickListener {
            showDatePicker(endDateInput)
        }
    }

    private fun showDatePicker(dateInput: EditText) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                dateInput.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            // Handle sending message here
            dismiss()
        }
    }

    companion object {
        fun newInstance(ownerName: String): ContactBottomSheet {
            return ContactBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("ownerName", ownerName)
                }
            }
        }
    }
} 