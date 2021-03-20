package aarnav100.developer.techjobs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.facebook.ads.NativeAd;
import com.google.gson.Gson;

public class MarkedActivity extends AppCompatActivity {

    public static final String MARKED = "MARKED";
    private JobAdapter jobAdapter;
    private NativeAd nativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marked);
        nativeAd = new NativeAd(this,"335152641143188_363415601650225");
        Gson gson = new Gson();
        ArrayList<Job> jobs = new ArrayList<>();
        SharedPreferences marked = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> st = marked.getStringSet(MarkedActivity.MARKED, new HashSet<String>());
        for (String s : st) {
            jobs.add(gson.fromJson(s, Job.class));
        }
        jobAdapter = new JobAdapter(this, jobs, nativeAd, true);
        jobAdapter.setArray(jobs);
        EmptyRecyclerView listView = findViewById(R.id.listview);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setEmptyView(findViewById(R.id.empty_view));
        listView.setAdapter(jobAdapter);
    }
}