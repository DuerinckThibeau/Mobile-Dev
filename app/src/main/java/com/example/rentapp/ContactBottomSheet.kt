package com.example.rentapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
            val startDate = startDateInput.text.toString()
            val endDate = endDateInput.text.toString()
            val message = messageInput.text.toString()

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(context, "Please select dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(context, "Please log in again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            
            // Get current user's details
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { userDoc ->
                    val firstName = userDoc.getString("firstname") ?: ""
                    val lastName = userDoc.getString("lastname") ?: ""
                    val profilePic = userDoc.getString("profilepicture") ?: ""

                    // Get item details
                    arguments?.getString("itemId")?.let { itemId ->
                        db.collection("items").document(itemId)
                            .get()
                            .addOnSuccessListener { itemDoc ->
                                val rental = hashMapOf(
                                    "itemId" to itemId,
                                    "itemTitle" to itemDoc.getString("title"),
                                    "itemImage" to itemDoc.getString("imageUrl"),
                                    "requestedById" to currentUser.uid,
                                    "requestedByName" to "$firstName $lastName",
                                    "requestedByProfilePic" to profilePic,
                                    "ownerId" to itemDoc.getString("createdById"),
                                    "startDate" to startDate,
                                    "endDate" to endDate,
                                    "message" to message,
                                    "status" to "PENDING"
                                )

                                db.collection("rentals")
                                    .add(rental)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Request sent successfully", Toast.LENGTH_SHORT).show()
                                        dismiss()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                    }
                }
        }
    }

    companion object {
        fun newInstance(ownerName: String, itemId: String): ContactBottomSheet {
            return ContactBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("ownerName", ownerName)
                    putString("itemId", itemId)
                }
            }
        }
    }
} 