package com.evgeniysharafan.utils.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.evgeniysharafan.utils.R;
import com.evgeniysharafan.utils.Res;
import com.evgeniysharafan.utils.Utils;
import com.evgeniysharafan.utils.ui.fragment.DebugSettingsFragment;

@SuppressWarnings("unused")
public class DebugSettingsActivity extends AppCompatActivity {

    public static void launch(Activity activity, @Nullable String... emailsForSending) {
        activity.startActivity(getIntent(activity, emailsForSending));
    }

    public static Intent getIntent(Activity activity, @Nullable String... emailsForSending) {
        return new Intent(activity, DebugSettingsActivity.class)
                .putExtra(DebugSettingsFragment.ARG_EMAILS, emailsForSending);
    }

    public static void addDebugSettingsIfNeeded(Activity activity, PreferenceScreen screen,
                                                boolean showInRelease, @Nullable String... emailsForSending) {
        if (Utils.isDebug() || showInRelease) {
            Preference debugSettings = new Preference(activity);
            debugSettings.setKey(Res.getString(R.string.key_debug_settings));
            debugSettings.setTitle(R.string.title_debug_settings);
            debugSettings.setIntent(getIntent(activity, emailsForSending));
            debugSettings.setSummary(R.string.summary_debug_settings);
            screen.addPreference(debugSettings);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_fragment);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.content, DebugSettingsFragment
                            .newInstance(getIntent().getStringArrayExtra(DebugSettingsFragment.ARG_EMAILS)),
                    DebugSettingsFragment.class.getSimpleName()).commit();
        }
    }

}
