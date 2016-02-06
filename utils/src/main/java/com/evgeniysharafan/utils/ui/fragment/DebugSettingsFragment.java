package com.evgeniysharafan.utils.ui.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.evgeniysharafan.utils.DeviceInfo;
import com.evgeniysharafan.utils.L;
import com.evgeniysharafan.utils.R;
import com.evgeniysharafan.utils.Res;
import com.evgeniysharafan.utils.Utils;

import static android.preference.Preference.OnPreferenceChangeListener;
import static android.preference.Preference.OnPreferenceClickListener;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DebugSettingsFragment extends PreferenceFragment implements OnPreferenceClickListener,
        OnPreferenceChangeListener {

    public static final String ARG_WRITE_IN_RELEASE = "arg_write_in_release";
    public static final String ARG_EMAILS = "arg_emails";

    private Preference sendDeviceInfo;
    private SwitchPreference writeLogs;
    private Preference sendLogs;
    private Preference clearLogs;
    private Preference versionName;
    private Preference versionCode;

    public static DebugSettingsFragment newInstance(boolean writeInRelease, @Nullable String... emailsForSending) {
        DebugSettingsFragment fragment = new DebugSettingsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_WRITE_IN_RELEASE, writeInRelease);
        args.putStringArray(ARG_EMAILS, emailsForSending);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.debug_preferences);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        prepareActionBar();
        findPreferences();
        fillSummaries();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void prepareActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_debug_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void findPreferences() {
        sendDeviceInfo = findPreference(Res.getString(R.string.key_debug_send_device_info));
        sendDeviceInfo.setOnPreferenceClickListener(this);

        writeLogs = (SwitchPreference) findPreference(Res.getString(R.string.key_debug_write_logs_to_file));
        writeLogs.setOnPreferenceChangeListener(this);

        sendLogs = findPreference(Res.getString(R.string.key_debug_send_logs));
        sendLogs.setOnPreferenceClickListener(this);

        clearLogs = findPreference(Res.getString(R.string.key_debug_clear_logs_folder));
        clearLogs.setOnPreferenceClickListener(this);

        versionName = findPreference(Res.getString(R.string.key_debug_version_name));
        versionCode = findPreference(Res.getString(R.string.key_debug_version_code));
    }

    private void fillSummaries() {
        versionName.setSummary(Utils.getVersionName());
        versionCode.setSummary(String.valueOf(Utils.getVersionCode()));
    }

    private void update() {
        writeLogs.setChecked(L.isNeedWriteToFile());

        if (writeLogs.isChecked()) {
            sendLogs.setSummary(R.string.summary_debug_disable_writing_logs);
            clearLogs.setSummary(R.string.summary_debug_disable_writing_logs);

            sendLogs.setEnabled(false);
            clearLogs.setEnabled(false);
        } else {
            int quantity = L.getLogsQuantity();
            if (quantity == 0) {
                sendLogs.setSummary(R.string.summary_debug_logs_folder_empty);
                clearLogs.setSummary(R.string.summary_debug_logs_folder_empty);
            } else {
                sendLogs.setSummary(Res.getQuantityString(R.plurals.summary_debug_send_logs, quantity, quantity));
                clearLogs.setSummary(Res.getQuantityString(R.plurals.summary_debug_clear_logs_folder, quantity, quantity));
            }

            sendLogs.setEnabled(quantity > 0);
            clearLogs.setEnabled(quantity > 0);
        }
    }

    private boolean getWriteInRelease() {
        return getArguments().getBoolean(ARG_WRITE_IN_RELEASE);
    }

    private String[] getEmailsForSending() {
        return getArguments().getStringArray(ARG_EMAILS);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(Res.getString(R.string.key_debug_send_device_info))) {
            DeviceInfo.sendDeviceInfoToEmail(getActivity(), getEmailsForSending());
        } else if (key.equals(Res.getString(R.string.key_debug_send_logs))) {
            L.sendLogsToEmail(getActivity(), getEmailsForSending());
        } else if (key.equals(Res.getString(R.string.key_debug_clear_logs_folder))) {
            L.clearLogsFolder();
            update();
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(Res.getString(R.string.key_debug_write_logs_to_file))) {
            L.setNeedWriteToFile((Boolean) newValue, getWriteInRelease());
            update();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
