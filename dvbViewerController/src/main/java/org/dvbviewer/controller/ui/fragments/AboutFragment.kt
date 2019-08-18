package org.dvbviewer.controller.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify.TransformFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.util.LinkifyCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_about.*
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.api.APIClient
import org.dvbviewer.controller.data.api.ApiResponse
import org.dvbviewer.controller.data.api.ApiStatus
import org.dvbviewer.controller.data.api.DMSInterface
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.data.version.VersionViewModel
import org.dvbviewer.controller.data.version.VersionViewModelFactory
import org.dvbviewer.controller.ui.base.BaseFragment
import java.util.regex.Pattern


class AboutFragment : BaseFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val versionName = activity?.packageManager?.getPackageInfo(activity?.packageName!!, 0)?.versionName
        versionTextView.text = versionName
        val dmsInterface = APIClient.client.create(DMSInterface::class.java)
        val repo = VersionRepository(context!!, dmsInterface)
        val vFac = VersionViewModelFactory(activity!!.application, repo)
        val versionViewModel = ViewModelProvider(this, vFac)
                .get(VersionViewModel::class.java)
        val versionObserver = Observer<ApiResponse<Boolean>> { response ->
            if (response?.status == ApiStatus.SUCCESS && response.data == true) {
                donationRow.visibility = View.VISIBLE
            }
        }
        versionViewModel.isSupported(MINIMUM_VERSION).observe(this, versionObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paypalButton.setOnClickListener { activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.donation_link)))) }
        val pattern = Pattern.compile(".")
        val myTransformFilter = TransformFilter { _, url ->
            url.substring(0, url.length-1)
        }
        LinkifyCompat.addLinks(privacyLabelTextView, pattern, getString(R.string.privacy_link), null, myTransformFilter)
    }

    companion object {
        const val MINIMUM_VERSION = "1.33.0.0"
    }

}