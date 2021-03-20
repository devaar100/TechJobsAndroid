package aarnav100.developer.techjobs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        findViewById(R.id.gmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(Intent.ACTION_SENDTO);
                mIntent.setData(Uri.parse("mailto:"));
                mIntent.putExtra(Intent.EXTRA_EMAIL  , new String[] {"aarnavjindal1000@gmail.com"});
                mIntent.putExtra(Intent.EXTRA_SUBJECT, "TechJobs Query");
                startActivity(Intent.createChooser(mIntent, "Send Email Using..."));
            }
        });
        findViewById(R.id.linkedin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/aarnavjindal/")));
            }
        });
    }
}