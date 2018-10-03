package org.dvbviewer.controller.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.text.util.LinkifyCompat
import android.text.util.Linkify.TransformFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_about.*
import org.dvbviewer.controller.R
import org.dvbviewer.controller.data.ApiResponse
import org.dvbviewer.controller.data.Status
import org.dvbviewer.controller.data.version.VersionRepository
import org.dvbviewer.controller.data.version.VersionViewModel
import org.dvbviewer.controller.data.version.VersionViewModelFactory
import org.dvbviewer.controller.io.api.APIClient
import org.dvbviewer.controller.io.api.DMSInterface
import java.util.regex.Matcher
import java.util.regex.Pattern


class AboutFragment : Fragment() {

    private val TAG = AboutFragment::class.java.simpleName
    private val MINIMUM_VERSION = "2.1.0.0"

    internal lateinit var dmsInterface: DMSInterface
    internal lateinit var versionViewModel: VersionViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            val activity = activity
            if (activity != null) {
                val versionName = activity.packageManager
                        .getPackageInfo(activity.packageName, 0)
                        .versionName
                versionTextView.text = versionName
            }
        } catch (e: NameNotFoundException) {
            Log.e(TAG, "Error getting version name", e)
        }

        dmsInterface = APIClient.getClient().create(DMSInterface::class.java)
        val repo = VersionRepository(context!!, dmsInterface)
        val vFac = VersionViewModelFactory(activity!!.application, repo)
        versionViewModel = ViewModelProviders.of(this, vFac)
                .get(VersionViewModel::class.java)
        val versionObserver = Observer<ApiResponse<Boolean>> { response ->
            if (response?.status == Status.SUCCESS && response.data == true) {
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
        val myTransformFilter = object : TransformFilter {
            override fun transformUrl(match: Matcher, url: String): String {
                return url.substring(0, url.length-1) //remove the $ sign
            }
        }
        LinkifyCompat.addLinks(privacyLabelTextView, pattern, getString(R.string.privacy_link), null, myTransformFilter)
    }

}