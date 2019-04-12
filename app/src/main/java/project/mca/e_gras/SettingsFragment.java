package project.mca.e_gras;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import project.mca.e_gras.util.MyUtil;

import android.util.DisplayMetrics;

import java.util.Locale;


public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String TAG = "MY-APP";

    private SharedPreferences mPreferences;
    private Preference langPref;
    private Preference themePref;

    public SettingsFragment() {
    }


    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String langPrefValue = mPreferences.getString("lang", getString(R.string.lang_pref_default_value));
        final String themePrefValue = mPreferences.getString("theme", getString(R.string.theme_pref_default_value));


        langPref = findPreference(getString(R.string.lang_pref_key));
        langPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String data = (String) o;

                if (!data.equals(langPrefValue)) {
                    setLanguageSummary((String) o);
                    changeLanguage((String) o);
                }

                return true;
            }
        });

        themePref = findPreference(getString(R.string.theme_pref_key));
        themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                setThemeSummary((String) o);
                changeTheme((String) o);
                return true;
            }
        });


        // set the summaries for language and theme pref
        setLanguageSummary(langPrefValue);
        setThemeSummary(themePrefValue);
    }


    private void changeLanguage(String langValue) {
        Locale newLocal = new Locale(langValue);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = newLocal;
        res.updateConfiguration(conf, dm);

        // restart the application
        MyUtil.showBottomDialog(getActivity(), getString(R.string.app_restart_info),
                true);
    }


    private void changeTheme(String themeValue) {
        switch (themeValue) {
            case "lt":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case "dk":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

        }

        // recreate the activity
        getActivity().recreate();
    }


    private void setThemeSummary(String s) {
        String value = "";

        switch (s) {
            case "lt":
                value = "Light";
                break;

            case "dk":
                value = "Dark";
                break;
        }

        themePref.setSummary(value);
    }


    private void setLanguageSummary(String s) {
        String value = "";

        switch (s) {
            case "en":
                value = "English";
                break;

            case "bn":
                value = getString(R.string.as_lang_label);
                break;
        }

        langPref.setSummary(value);
    }
}
