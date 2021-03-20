package aarnav100.developer.techjobs;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdView;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

public class JobAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity activity;
    private ArrayList<Job> jobs;
    private View.OnClickListener ocl, ocl2;
    private View adView;
    private int adLoaded = 0;
    private Gson gson;

    public JobAdapter(final Activity activity, final ArrayList<Job> jobs, final NativeAd nativeAd, final boolean isDelete) {
        gson = new Gson();
        this.activity = activity;
        this.ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (Integer)view.getTag();
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getJobs().get(pos).getUrl())));
            }};
        this.ocl2 = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int pos = (Integer)view.getTag();
                if(isDelete){
                    new AlertDialog.Builder(activity)
                            .setTitle("Remove from marked")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    removeFromMarked(gson.toJson(getJobs().get(pos)),pos);
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                } else{
                    String job = gson.toJson(getJobs().get(pos));
                    updateMarked(job);
                }
            }
        };
        this.jobs = jobs;
        NativeAdListener listener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }
            @Override
            public void onError(Ad ad, AdError adError) {
            }
            @Override
            public void onAdLoaded(Ad ad) {
                adView = NativeAdView.render(activity, nativeAd);
                adLoaded = 1;
                notifyDataSetChanged();
            }
            @Override
            public void onAdClicked(Ad ad) {
            }
            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };
        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(listener).build());
    }

    private void removeFromMarked(String job, int pos){
        SharedPreferences marked = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> st = marked.getStringSet(MarkedActivity.MARKED, new HashSet<String>());
        st.remove(job);
        SharedPreferences.Editor editor = marked.edit();
        editor.putStringSet(MarkedActivity.MARKED, st);
        editor.apply();
        Toast.makeText(activity, "Job removed from marked tab", Toast.LENGTH_SHORT).show();
        getJobs().remove(pos);
        notifyDataSetChanged();
    }

    private void updateMarked(String job){
        SharedPreferences marked = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> st = marked.getStringSet(MarkedActivity.MARKED, new HashSet<String>());
        st.add(job);
        SharedPreferences.Editor editor = marked.edit();
        editor.putStringSet(MarkedActivity.MARKED, st);
        editor.apply();
        Toast.makeText(activity, "Job saved to marked tab", Toast.LENGTH_SHORT).show();
    }

    private ArrayList<Job> getJobs(){
        return jobs;
    }

    public void setArray(ArrayList<Job> jobs){
        this.jobs = jobs;
        this.jobs.add(0, new Job());
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==0){
            LayoutInflater li = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = li.inflate(R.layout.ad_view,parent,false);
            return new AdViewHolder(itemView);
        } else{
            LayoutInflater li = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = li.inflate(R.layout.job_view,parent,false);
            return new JobViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType()==1){
            final Job job = jobs.get(position);
            ((JobViewHolder)holder).company.setText(job.getCompany());
            ((JobViewHolder)holder).position.setText(job.getPosition());
            ((JobViewHolder)holder).location.setText(job.getLocation());
            ((JobViewHolder)holder).date.setText("Date added: " + DateFormat.getDateInstance().format(new Date(job.getDate()*1000)));
            ((JobViewHolder)holder).desc.setText(job.getDesc());
            Picasso.get().load("https://tech-jobs.in/"+job.getImage()).into(((JobViewHolder)holder).img);
            ((JobViewHolder)holder).apply.setTag(position);
            ((JobViewHolder)holder).apply.setOnClickListener(ocl);
            ((JobViewHolder)holder).mark.setTag(position);
            ((JobViewHolder)holder).mark.setOnClickListener(ocl2);
        } else {
            if(adLoaded==1) {
                ((AdViewHolder)holder).parent.removeAllViews();
                ((AdViewHolder)holder).parent.addView(adView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                adLoaded = 2;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    class AdViewHolder extends RecyclerView.ViewHolder{
        private ViewGroup parent;
        AdViewHolder(View itemView){
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
        }
    }

    class JobViewHolder extends RecyclerView.ViewHolder{
        private TextView company, desc, location, position, date;
        private ImageView img, mark;
        private Button apply;
        JobViewHolder(View itemView) {
            super(itemView);
            company = itemView.findViewById(R.id.company);
            position = itemView.findViewById(R.id.position);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            desc = itemView.findViewById(R.id.desc);
            apply = itemView.findViewById(R.id.apply);
            img = itemView.findViewById(R.id.img);
            mark = itemView.findViewById(R.id.mark);
        }
    }
}
