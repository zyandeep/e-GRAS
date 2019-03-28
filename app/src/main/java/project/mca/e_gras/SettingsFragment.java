package project.mca.e_gras;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;


public class SettingsFragment extends PreferenceFragmentCompat {

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
        getActivity().recreate();

        ((SettingsActivity) getActivity()).setLanguageChanged(true);
    }


    private void changeTheme(String themeValue) {
        switch (themeValue) {
            case "lt":

                ((AppCompatActivity)getContext())
                        .getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                break;

            case "dk":

                ((AppCompatActivity)getContext())
                        .getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                break;

        }
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
                value = "Assamese";
                break;
        }

        langPref.setSummary(value);
    }
}
