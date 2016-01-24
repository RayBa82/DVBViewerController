package org.dvbviewer.controller.ui.fragments;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.dvbviewer.controller.R;

public class AboutFragment extends Fragment{
	
	private TextView	versionTextView;
	private ImageButton	payPalButton;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String versionName = "";
		try {
			versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		versionTextView.setText(versionName);
		payPalButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XBZT782XQV7AY")));

			}
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_about, container, false);
		payPalButton = (ImageButton) v.findViewById(R.id.paypalButton);
		versionTextView = (TextView) v.findViewById(R.id.versionTextView);
		return v;
	}

}
