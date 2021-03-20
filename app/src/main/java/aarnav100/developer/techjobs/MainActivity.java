package aarnav100.developer.techjobs;

import aarnav100.developer.techjobs.TagView.Tag;
import aarnav100.developer.techjobs.TagView.TagView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAd;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.objects.Update;
import com.github.okdroid.checkablechipview.CheckableChipView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference jobDB,companyDB;
    private ArrayList<Job> jobs;
    private AlertDialog mDialog, filterDialog;
    private JobAdapter jobAdapter;
    private int year = 4;
    private Set<String> selectedCompanies;
    private List<Tag> tags;
    private TagView tagGroup;
    private CheckableChipView year2, year3, year4;
    private NativeAd nativeAd;
    private View loaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudienceNetworkAds.initialize(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        jobDB = database.getReference("jobs");
        companyDB = database.getReference("companies");
        jobs = new ArrayList<>();
        selectedCompanies = new HashSet<>();
        nativeAd = new NativeAd(this,"335152641143188_360900235235095");
        jobAdapter = new JobAdapter(this, jobs, nativeAd, false);
        EmptyRecyclerView listView = findViewById(R.id.listview);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setEmptyView(findViewById(R.id.empty_view));
        listView.setAdapter(jobAdapter);
        initLoader();
        initFilterDialog();
        selectedCompanies = new HashSet<>();
        getCompanies();
        mDialog.show();
    }

    private void initLoader(){
        loaderView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.loader_dialog,null);
        ((GifDrawable)((GifImageView)loaderView.findViewById(R.id.img)).getDrawable()).setSpeed(2f);
        mDialog = new AlertDialog.Builder(this)
                .setView(loaderView)
                .create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(false);
    }

    private void updateJobs(){
        ArrayList<Job> filteredJobs = new ArrayList<>();
        for(Job job: jobs){
            if(job.getYear()==year){
                if(selectedCompanies.size()!=0){
                    if(selectedCompanies.contains(job.getCompany())){
                        filteredJobs.add(job);
                    }
                } else{
                    filteredJobs.add(job);
                }
            }
        }
        jobAdapter.setArray(filteredJobs);
    }

    private void updateYear(int year){
        if(year!=this.year){
            if(this.year==2) year2.setChecked(false);
            else if(this.year==3) year3.setChecked(false);
            else year4.setChecked(false);

            if(year==2) year2.setChecked(true);
            else if(year==3) year3.setChecked(true);
            else year4.setChecked(true);

            this.year = year;
        }
    }

    private void initFilterDialog(){
        View filterView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.filter_dialog,null);
        year2 = filterView.findViewById(R.id.year2);
        year3 = filterView.findViewById(R.id.year3);
        year4 = filterView.findViewById(R.id.year4);
        year2.setOnCheckedChangeListener(new Function2<CheckableChipView, Boolean, Unit>() {
            @Override
            public Unit invoke(CheckableChipView checkableChipView, Boolean aBoolean) {
                updateYear(2);
                return null;
            }
        });
        year3.setOnCheckedChangeListener(new Function2<CheckableChipView, Boolean, Unit>() {
            @Override
            public Unit invoke(CheckableChipView checkableChipView, Boolean aBoolean) {
                updateYear(3);
                return null;
            }
        });
        year4.setOnCheckedChangeListener(new Function2<CheckableChipView, Boolean, Unit>() {
            @Override
            public Unit invoke(CheckableChipView checkableChipView, Boolean aBoolean) {
                updateYear(4);
                return null;
            }
        });
        year4.setChecked(true);
        tagGroup = filterView.findViewById(R.id.tag_group);
        tagGroup.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(final Tag tag, int position) {
                if(selectedCompanies.contains(tag.getText())){
                    selectedCompanies.remove(tag.getText());
                    tag.setLayoutColor(getResources().getColor(R.color.white));
                    tag.setTagTextColor(getResources().getColor(R.color.colorAccent));
                } else{
                    selectedCompanies.add(tag.getText());
                    tag.setLayoutColor(getResources().getColor(R.color.colorAccent));
                    tag.setTagTextColor(getResources().getColor(R.color.white));
                }
                tagGroup.drawTags();
            }
        });
        filterDialog = new AlertDialog.Builder(this)
                .setView(filterView)
                .create();
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateJobs();
                filterDialog.dismiss();
            }
        });
        filterDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                updateJobs();
            }
        });
        filterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                updateJobs();
            }
        });
    }

    private void getCompanies(){
        companyDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<String>> values = new GenericTypeIndicator<ArrayList<String>>() {};
                ArrayList<String> companies = dataSnapshot.getValue(values);
                tags = new ArrayList<>();
                for(String company: companies){
                    Tag tag = new Tag(company);
                    tag.setLayoutColor(getResources().getColor(R.color.white));
                    tag.setTagTextColor(getResources().getColor(R.color.colorAccent));
                    tag.setLayoutBorderColor(getResources().getColor(R.color.colorAccent));
                    tag.setLayoutBorderSize(1);
                    tags.add(tag);
                }
                tagGroup.addTags(tags);
                getJobs();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "There was an error. Please try again later...", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }

    private void getJobs(){
        jobDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ArrayList<Job>> values = new GenericTypeIndicator<ArrayList<Job>>() {};
                jobs = dataSnapshot.getValue(values);
                mDialog.dismiss();
                updateJobs();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "There was an error. Please try again later...", Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(nativeAd!=null){
            nativeAd.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        if(item.getItemId()==R.id.filter){
            filterDialog.show();
            filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (3*height)/4);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.signin:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.contact:
                startActivity(new Intent(MainActivity.this, ContactActivity.class));
                break;
            case R.id.marked:
                startActivity(new Intent(MainActivity.this, MarkedActivity.class));
                break;
            case R.id.share:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "TechJobs App");
                i.putExtra(Intent.EXTRA_TEXT   , "https://play.google.com/store/apps/details?id=aarnav100.developer.techjobs&hl=en");
                startActivity(i);
                break;
            case R.id.rate:
                i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=aarnav100.developer.techjobs&hl=en"));
                startActivity(i);
                break;
            case R.id.update:
                checkForUpdate();
                break;
        }
        return true;
    }

    private void showDialog(String txt){
        mDialog.show();
        ((TextView)loaderView.findViewById(R.id.text)).setText(txt);
    }

    private void checkForUpdate(){
        showDialog("Fetching Updates");
        AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                .withListener(new AppUpdaterUtils.UpdateListener() {
                    @Override
                    public void onSuccess(Update update, Boolean isUpdateAvailable) {
                        final AlertDialog updateDialog = new AlertDialog.Builder(MainActivity.this).create();
                        if(isUpdateAvailable){
                            updateDialog.setTitle("Update available");
                            updateDialog.setMessage("New version is now available. Updates offer more interesting features and bug fixes. Would you like to update the app ?");
                            updateDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface updateDialogInterface, int i) {
                                    updateDialogInterface.dismiss();
                                }
                            });
                            updateDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface updateDialogInterface, int i) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=aarnav100.developer.techjobs&hl=en")));
                                    updateDialogInterface.dismiss();
                                }
                            });
                        } else{
                            updateDialog.setTitle("No update available");
                            updateDialog.setMessage("You have the latest version of TechJobs on your device");
                            updateDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface updateDialogInterface, int i) {
                                    updateDialogInterface.dismiss();
                                }
                            });
                        }
                        mDialog.dismiss();
                        updateDialog.show();
                    }
                    @Override
                    public void onFailed(AppUpdaterError error) {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
        appUpdaterUtils.start();
    }
}