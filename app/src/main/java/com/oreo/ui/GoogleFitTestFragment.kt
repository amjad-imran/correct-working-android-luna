package com.oreo.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.noisefit.luna.databinding.FragmentGooglFitTestBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.LOGS
import java.util.concurrent.TimeUnit


class GoogleFitTestFragment :
    BaseFragment<FragmentGooglFitTestBinding>(FragmentGooglFitTestBinding::inflate) {

    val TAG = "GoogleFitTestFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                2233
            )
        }
    }



    override fun initListener() {
        binding.bSignIn.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Fitness.SCOPE_ACTIVITY_READ)
                .build()

            val account = context?.let { it1 -> GoogleSignIn.getLastSignedInAccount(it1) }

            if (account == null) {
                // User is not signed in, request sign-in
                val signInIntent = GoogleSignIn.getClient(requireContext(), options).signInIntent
                startActivityForResult(
                    signInIntent,
                    22
                ) // You'll need to handle the result in onActivityResult
            }else{
                context.showShortToast("User already signed in ${account.email}")
            }
        }

        binding.bLogout.setOnClickListener {
            Fitness.getConfigClient(requireContext(),  GoogleSignIn.getAccountForExtension(requireContext(), getFitnessOptions()))
                .disableFit()
                .addOnSuccessListener {
                    LOGS.d(TAG,"Disabled Google Fit")
                }
                .addOnFailureListener { e ->
                    LOGS.d(TAG,"There was an error disabling Google Fit $e")
                }
        }

        binding.bRequest.setOnClickListener {
            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_WORKOUT_EXERCISE)
                .setTimeRange(1696918645 , System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build()

            val lastSignIn = context?.let { it1 -> GoogleSignIn.getLastSignedInAccount(it1) }

            Fitness.getHistoryClient(
                requireActivity(),
                lastSignIn!!
            )
                .readData(readRequest)
                .addOnSuccessListener { dataReadResponse: DataReadResponse? ->
                    LOGS.d("GoogleFitTestFragment","DataSET $dataReadResponse")
                }
                .addOnFailureListener { e: Exception? ->
                    LOGS.w("GoogleFitTestFragment","Failure ${e?.message}")
                    e?.printStackTrace()
                }


        }
    }

    fun getFitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
            .build()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 22) {
            val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }
            if (result!!.isSuccess) {
                context.showShortToast("Sign in Success")
                // User signed in, you can now access fitness data
            } else {
                context.showShortToast("Sign in Failed")
                // Handle sign-in failure
            }
        }
    }

    override fun subscribeObservers() {

    }


}