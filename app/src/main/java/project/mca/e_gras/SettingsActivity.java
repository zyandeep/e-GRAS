package project.mca.e_gras;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    private boolean languageChanged = false;

    public static final String KEY_LANG_CHANGED = "lang-changed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public void setLanguageChanged(boolean languageChanged) {
        this.languageChanged = languageChanged;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (languageChanged) {
            Intent data = new Intent();
            data.putExtra(KEY_LANG_CHANGED, languageChanged);
            setResult(RESULT_OK, data);

            finish();
        }
    }
}
