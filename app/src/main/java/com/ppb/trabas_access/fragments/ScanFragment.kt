package com.ppb.trabas_access.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.ppb.trabas_access.R
import com.ppb.trabas_access.databinding.FragmentScanBinding
import com.ppb.trabas_access.model.dao.TransactionHistory
import com.ppb.trabas_access.model.dao.Users
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private lateinit var surfaceView: SurfaceView
    private lateinit var cameraSource: CameraSource
    private lateinit var qrCodeDetector: BarcodeDetector
    private lateinit var transactionHistoryRef: DatabaseReference
    private var isScanningEnabled = true
    private var isDialogShowing = false
    private var isProcessingPayment = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        surfaceView = binding.surfaceView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupQRCodeScanner()
    }

    private fun setupQRCodeScanner() {
        qrCodeDetector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        if (!qrCodeDetector.isOperational) {
            showQRScannerError()
            return
        }

        cameraSource = CameraSource.Builder(requireContext(), qrCodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        cameraSource.start(holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    requestCameraPermission()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        qrCodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val qrCodes: SparseArray<Barcode> = detections.detectedItems

                if (qrCodes.size() > 0) {
                    val qrCodeValue = qrCodes.valueAt(0).displayValue
                    if (!isDialogShowing && !isProcessingPayment) { // Check if the payment success dialog is not showing
                        processQRPayment(qrCodeValue)
                    }
                }
            }
        })
    }

    private fun processQRPayment(qrCodeValue: String) {

        if (!isAdded || requireActivity().isFinishing) {
            // Fragment sudah dihancurkan atau activity sudah selesai, tidak perlu melanjutkan pemrosesan
            return
        }

        if (isProcessingPayment) {
            return
        }

        isProcessingPayment = true

        val qrCodeData = decodeQRCode(qrCodeValue)
        val transactionType = qrCodeData["type"]
        val ticketPrice = 3900L // Harga tiket ditentukan di sistem
        val timeStamp = getCurrentDateTimeFormatted()

        // pengecekan tipe transaksi
        if (transactionType == "TRABAS-ACCESS QR-PAYMENT") {
            // pengurangan saldo pengguna
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                val userRef = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)

                userRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {

                        val userSnapshot = currentData.getValue(Users::class.java)
                        transactionHistoryRef = FirebaseDatabase.getInstance().reference.child("transactionHistory")

                        // Pastikan pengguna ada dan memiliki saldo yang cukup
                        if (userSnapshot != null && userSnapshot.balance!! >= ticketPrice) {
                            // Lakukan pengurangan saldo
                            userSnapshot.balance = userSnapshot.balance!! - ticketPrice
                            currentData.value = userSnapshot

                            // Simpan data transaksi ke tabel transactionHistory
                            val transactionID = generateRandomTransactionID() // Generate transactionID secara random
                            val transaction = TransactionHistory(user.uid, user.email, transactionID, ticketPrice, timeStamp, "SUCCESS")
                            transactionHistoryRef.child(transactionID).setValue(transaction)

                            qrCodeDetector.release()

                            // Transaksi berhasil, kembalikan Result.success()
                            return Transaction.success(currentData)
                        } else {

                            // Jika saldo tidak cukup, set status transaksi menjadi "FAILED"
                            val transactionID =
                                generateRandomTransactionID() // Generate transactionID secara random
                            val transaction = TransactionHistory(
                                user.uid,
                                user.email,
                                transactionID,
                                ticketPrice,
                                timeStamp,
                                "FAILED"
                            )
                            transactionHistoryRef.child(transactionID).setValue(transaction)

                            // Kembalikan Result.abort() untuk menandakan transaksi gagal
                            return Transaction.abort()
                        }
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        activity?.runOnUiThread {
                            if (committed && error == null) {
                                if ((currentData != null) && currentData.exists()) {
                                    // Transaksi berhasil
                                    showToast("Pembayaran berhasil!")
                                    isScanningEnabled = false
                                    isDialogShowing = true
                                    showPaymentSuccessDialog()
                                    closeScanFragment()
                                }
                            } else {
                                // Transaksi gagal karena error lain (misalnya koneksi ke database terputus)
                                showToast("Terjadi kesalahan saat melakukan pembayaran.")
                                showToast("Saldo tidak cukup untuk pembayaran!")
                                isScanningEnabled = false
                                closeScanFragment()
                            }
                        }
                    }
                })
            }
        } else {
            requireActivity().runOnUiThread {
                showToast("Pembayaran Tidak Berhasil.")
                isScanningEnabled = false
                isProcessingPayment = true
                closeScanFragment()
            }
        }

    }

    private fun closeScanFragment() {
        requireActivity().runOnUiThread {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }
    }

    private fun showPaymentSuccessDialog() {
        if (isAdded) { // Check if the Fragment is attached to the Activity
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.bottom_sheet_payment_success)
            dialog.window?.apply {
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawable(ColorDrawable(Color.WHITE))
                attributes.windowAnimations = R.style.DialogAnimation
                setGravity(Gravity.CENTER)
            }

            val closeButton: Button = dialog.findViewById(R.id.btn_close)
            closeButton.setOnClickListener {
                dialog.dismiss()
                isScanningEnabled = false
                isDialogShowing = false
            }

            dialog.show()
        }
    }


    private fun generateRandomTransactionID(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')

        // generate max 6 characters
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun getCurrentDateTimeFormatted(): String {
        val currentDateTime = Date()
        val dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        return dateFormatter.format(currentDateTime)
    }

    private fun decodeQRCode(qrCodeValue: String): Map<String, String> {

        val dataMap = mutableMapOf<String, String>()
        val dataParts = qrCodeValue.split(" ")

        if (dataParts.size == 3 && dataParts[0] == "TRABAS-ACCESS" && dataParts[1] == "QR-PAYMENT") {
            dataMap["type"] = dataParts[0] + " " + dataParts[1]
            dataMap["transaction_id"] = dataParts[2]
        } else {
            showToast("QR Code tidak valid. Pastikan QR Code yang Anda pindai benar.")
            isProcessingPayment = true
        }

        return dataMap
    }

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
            isProcessingPayment = false
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    private fun showQRScannerError() {

        Snackbar.make(requireView(), "QR Code Scanner tidak tersedia", Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") {
                setupQRCodeScanner()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (isScanningEnabled && isDialogShowing) {
            // If scanning is enabled and the dialog was showing, restart scanning
            setupQRCodeScanner()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isDialogShowing) {
            // If the dialog is showing, stop scanning
            qrCodeDetector.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        qrCodeDetector.release()
    }

}