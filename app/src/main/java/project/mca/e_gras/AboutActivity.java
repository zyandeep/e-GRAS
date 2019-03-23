package project.mca.e_gras;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // version info
        Element versionElement = new Element();
        versionElement.setTitle("Version 1.0");

        // developer info
        Element developerElement = new Element();
        developerElement.setTitle("Developed by NIC");
        developerElement.setIconDrawable(R.drawable.ic_code);


        // about page view
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.image)
                .setDescription(getResources().getString(R.string.about_page_description))
                .addItem(versionElement)
                .addItem(developerElement)
                .addGroup("Connect with us")
                .addWebsite("https://assamegras.gov.in/")
                .create();

        setContentView(aboutPage);
    }
}
