package edu.orangecoastcollege.cs273.dnovasky.flagquiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    // Creates preferences GUI from the preferences.xml file in res/xml
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferrences); // Load from XML
    }
}
