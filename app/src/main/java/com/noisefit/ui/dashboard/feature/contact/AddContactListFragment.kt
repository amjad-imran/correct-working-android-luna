package com.noisefit.ui.dashboard.feature.contact

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback

import com.noisefit.luna.databinding.FragmentAddContactListBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.databinding.LayoutCustomAlertBinding
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.models.Contact
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint

const val PERMISSIONS_REQUEST_READ_CONTACTS = 111
const val ADD_CONTACT_KEY = "ADD_CONTACT_KEY"

@AndroidEntryPoint
class AddContactListFragment :
    BaseBottomSheet<FragmentAddContactListBinding>(FragmentAddContactListBinding::inflate),
    AddContactListAdapter.ContactInteractionListener {

    val viewModel: AddContactListViewModel by viewModels()

    private var maxContactCount = 0

    private val contactAdapter: AddContactListAdapter by lazy {
        AddContactListAdapter(requireContext(), this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val contactList =
                ArrayList(AddContactListFragmentArgs.fromBundle(it).contactList!!.toList())
            viewModel.contactCount = contactList.size
            maxContactCount = AddContactListFragmentArgs.fromBundle(it).maxCount
            viewModel.setContact(contactList)
        }
        subscribeObservers()
        setUpRecyclerView()
        initListener()
        checkContactsPermission()
    }

    private fun setUpRecyclerView() {
        binding.rv.adapter = contactAdapter
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        //   binding.rv.addItemDecoration(MarginItemDecoration(16))
    }


    private fun checkContactsPermission() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showPermissionDenialDialog(false)
        } else fetchPhoneContacts()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS
            && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchPhoneContacts()
        } else {
            showPermissionDenialDialog(true)
        }
    }


    private fun fetchPhoneContacts() {
        binding.textView19.visible()
        binding.etSearch.visible()
        binding.rv.visible()
        binding.btnAllow.visible()
        binding.btnCancel.visible()
        binding.lytNoPermission.root.gone()
        viewModel.fetchPhoneContacts()
    }

    private fun showPermissionDenialDialog(isPermissionDenial: Boolean) {


        binding.textView19.invisible()
        binding.etSearch.invisible()
        binding.rv.invisible()
        binding.btnAllow.invisible()
        binding.btnCancel.invisible()


        binding.lytNoPermission.apply {
            tvTitle.text = getString(R.string.text_title_sync_contact_with_watch)
            tvDesc.text = getString(R.string.text_desc_sync_contact_with_watch)
            root.visible()
            btnDismiss.visible()
            tvDesc.makeLinks(
                true,
                Pair("Privacy Policy", View.OnClickListener {
                    activity?.let {
                        startActivity(
                            WebViewActivity.getStartIntent(
                                it,
                                getString(R.string.text_privacy_policy),
                                AppConstants.URL_PRIVACY_POLICY
                            )
                        )
                    }

                })
            )
            btnDismiss.setOnClickListener {
                dismiss()
            }
            btnContinue.setOnClickListener {
                if (isPermissionDenial) {
                    ShareUtil.openAppPermissionSettings(context)
                } else {
                    requestPermissions(
                        Array(1) { Manifest.permission.READ_CONTACTS },
                        PERMISSIONS_REQUEST_READ_CONTACTS
                    )
                }
            }
        }
    }

    private fun initListener() {
        binding.btnAllow.setOnClickListener {
            setFragmentResult(
                ADD_CONTACT_KEY,
                bundleOf("contacts" to viewModel.getContact())
            )
            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                contactAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}

        })

    }


    private fun subscribeObservers() {
        viewModel.noiseFitSearchQuery.observe(this) {
            val searchQuery = it
            contactAdapter.filter.filter(searchQuery)
        }

        viewModel.contactsLiveData.observe(this) {
            if (it.isEmpty()) {
//                binding.layoutNoContact.root.visible()
            } else {
//                binding.layoutNoContact.root.gone()
                contactAdapter.setDataSet(it)
            }

        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }


    private fun showMaxContactDialog() {
        val alertMessage = getString(R.string.text_max_contact_message, viewModel.contactCount)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                getString(
                    R.string.text_max_contact_reached
                )
            )
            .setCancelable(false)
            .setMessage(alertMessage)
            .setNegativeButton(resources.getString(R.string.text_close)) { dialog, which ->
                dialog.dismiss()

            }

            .show()

    }

    override fun onContactClick(
        contact: Contact,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    ) {


        if (viewModel.contactCount >= maxContactCount && isChecked) {
            checkBox.isChecked = false
            showMaxContactDialog()
        } else {
            if (isChecked) {
                viewModel.updateContact(contact)
                viewModel.contactCount += 1
            } else {
                viewModel.removeContact(contact)
                viewModel.contactCount -= 1
            }

            contactAdapter.updateContactList(position, isChecked)

        }
    }

    private fun showPermissionDialog(
        title: String,
        message: String?,
        doNotShow: Boolean,
        ctaText: String,
        callback: BinaryActionCallback?
    ): AlertDialog {
        var alert: AlertDialog? = null
        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.MaterialAlertDialog_rounded)
        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            com.noisefit_commans.R.layout.layout_custom_alert, null, false
        )

        layoutCustomAlertBinding.apply {
            tvTitle.text = title
            tvDesc.text = message
            btnAllow.text = ctaText
            if (doNotShow) {
                btnDoNotShowAgain.visible()
            }

            btnAllow.setOnClickListener {
                alert?.dismiss()
                callback?.yes()
            }
            btnCancel.setOnClickListener {
                alert?.dismiss()
                callback?.no()
            }
        }


        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)

        alert = builder.create()
        alert.show()
        return alert
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}