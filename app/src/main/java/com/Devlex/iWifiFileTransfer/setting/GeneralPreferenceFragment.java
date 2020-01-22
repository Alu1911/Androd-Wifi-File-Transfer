package com.Devlex.iWifiFileTransfer.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.Devlex.iWifiFileTransfer.R;

import static com.Devlex.iWifiFileTransfer.setting.SettingsActivity.KEY_FILE_HIDDEN;
import static com.Devlex.iWifiFileTransfer.setting.SettingsActivity.KEY_FILE_SIZE;
import static com.Devlex.iWifiFileTransfer.setting.SettingsActivity.KEY_FILE_THUMBNAIL;
import static com.Devlex.iWifiFileTransfer.setting.SettingsActivity.KEY_FOLDER_SIZE;
import static com.Devlex.iWifiFileTransfer.setting.SettingsActivity.KEY_RECENT_MEDIA;

public class GeneralPreferenceFragment extends PreferenceFragment implements OnPreferenceClickListener {
	
	public GeneralPreferenceFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findPreference(KEY_FILE_SIZE).setOnPreferenceClickListener(this);
		findPreference(KEY_FOLDER_SIZE).setOnPreferenceClickListener(this);
		findPreference(KEY_FILE_THUMBNAIL).setOnPreferenceClickListener(this);
		findPreference(KEY_FILE_HIDDEN).setOnPreferenceClickListener(this);
		findPreference(KEY_RECENT_MEDIA).setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		SettingsActivity.logSettingEvent(preference.getKey());
		return false;
	}
}