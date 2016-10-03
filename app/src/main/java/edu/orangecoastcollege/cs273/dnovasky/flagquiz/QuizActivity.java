package edu.orangecoastcollege.cs273.dnovasky.flagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class QuizActivity extends AppCompatActivity {
    // Keys for reading data from SharedPreferences
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true; // Used to force portrait mode.
    private boolean preferencesChanged = true; // Did preferences change?

    /**
     * onCreate generated the appropriate layout to inflate, depending on the screen size.
     * If the device is large or extra large in will load the content_main.xml(sw700dp-land) which
     * includes both the fragment_quiz.xml and the fragment_settings.xml. Otherwise it just
     * inflates the standard content_main.xml with the fragment_quiz.
     *
     * All default preferences are set using the preferences.xml file.
     * @param savedInstanceState the saved state to restore (not being used)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set default values n tha apps SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferrences, false);

        // Register listener for SharedPreferences changes.
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        // Determine screen size.
        int screenSize = getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;

        // If device is a tablet set phoneDevice to false
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false; // Not a phone sized device.

        if(phoneDevice)
            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * onStart is called after onCreate completes it's execution. This method will update
     * the number of guess rows to display and the regions to choose flags from, then resets
     * the quiz with the new preferences.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {
            // Now that the default preferences have been set,
            // initialize QuizActivityFragment and start the quiz.
            QuizActivityFragment quizFragment = (QuizActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
        }
    }

    /**
     * Shows the settings menu if the app is running on a phone or a portrait-oriented tablet
     * only. (Large screen sizes include the settings fragment in the layout)
     * @param menu The settings menu.
     * @return True if the settings menu was inflated false otherwise;
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        // Display the apps menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Inflate the menu.
            getMenuInflater().inflate(R.menu.menu_quiz, menu);
            return true;
        }
        else
            return false;
    }

    /**
     * Display the SettingsActivity when running on a phone or portrait-oriented tablet. Starts
     * the activity by use of an intent (No data is passed because the shared preference,
     * preference.xml has all the data necessary)
     *
     * @param item The menu item
     * @return True if an option item was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Listener to handle changes in the apps shared preferences (preferences.xml)
     *
     * If either the guess option or regions options are changed, teh quiz qill retstart
     * with the new settings.
     */
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String s) {
                    // Called when the user chnges the apps preferences
                    preferencesChanged = true; // user Changed app settings

                    QuizActivityFragment quizFragment = (QuizActivityFragment)
                            getSupportFragmentManager().findFragmentById(R.id.quizFragment);

                    if (s.equals(CHOICES)) { // # of choices to display changed
                        if (quizFragment != null) {
                            quizFragment.updateGuessRows(sharedPreferences);
                            quizFragment.resetQuiz();
                        }
                    }
                    else if (s.equals(REGIONS)) { // Regions to include changed
                        Set<String> regions =
                                sharedPreferences.getStringSet(REGIONS, null);

                        if (regions != null && regions.size() > 0) {
                            if(quizFragment != null) {
                                quizFragment.updateRegions(sharedPreferences);
                                quizFragment.resetQuiz();
                            }
                        }
                        else {
                            // Must select at least one region -- set North America as default
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            if (regions != null) {
                                regions.add(getString(R.string.default_region));
                            }
                            editor.putStringSet(REGIONS, regions);
                            editor.apply();

                            Toast.makeText(QuizActivity.this,
                                    R.string.default_region_message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    Toast.makeText(QuizActivity.this,
                            R.string.reset_quiz,
                            Toast.LENGTH_SHORT).show();
                }
            };
}
