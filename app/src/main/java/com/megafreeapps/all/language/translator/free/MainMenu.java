package com.megafreeapps.all.language.translator.free;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.navigation.NavigationView;
import com.megafreeapps.all.language.translator.free.conversation.ConversationActivity;

public class MainMenu extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    AdView mAdView, mAdViewDialog;
    int viewId = 0;
    private InterstitialAd mInterstitialAd;
    private Dialog mDialog;

    private void Init() {
        findViewById(R.id.iv_text_translation).setOnClickListener(this);
        findViewById(R.id.iv_voice_conversation).setOnClickListener(this);

        findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
                if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }

            }
        });
        findViewById(R.id.btn_rate_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i);
                }
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //This is the AppID for Startup Ads Banner
        setContentView(R.layout.main_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.ad_int_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                onClickListner();
            }
        });
        Init();

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getPackageName());
            if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        } else if (id == R.id.nav_more) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Mega+Free+Apps+Developers"));
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            }

        } else if (id == R.id.nav_rate) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
            if (i.resolveActivity(getPackageManager()) != null) {
                startActivity(i);
            }

        } else if (id == R.id.nav_privacy) {
            startActivity(new Intent(MainMenu.this, PrivacyPolicy.class));

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (mDialog != null && mDialog.isShowing()) {

            mDialog.dismiss();
        } else {
            SpeedometerTypeDialoge();
        }

    }

    private void SpeedometerTypeDialoge() {

        try {
            View view = LayoutInflater.from(MainMenu.this).inflate(R.layout.exit_dialog, null, false);
            mAdViewDialog =view.findViewById(R.id.adView_dialog);
            mAdViewDialog.loadAd(new AdRequest.Builder().build());
            mAdViewDialog.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdViewDialog.setVisibility(View.VISIBLE);
                }
            });
            view.findViewById(R.id.txt_rate_us).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intentMore = new Intent(Intent.ACTION_VIEW);
                        intentMore.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=en"));
                        if (intentMore.resolveActivity(getPackageManager()) != null) {
                            startActivity(intentMore);
                        }
                    } catch (Exception ignored) {
                    }
                    if (mAdViewDialog != null){
                        mAdViewDialog.destroy();
                    }
                    mDialog.dismiss();
                }
            });
            view.findViewById(R.id.txt_no).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                    if (mAdViewDialog != null){
                        mAdViewDialog.destroy();
                    }

                }
            });
            view.findViewById(R.id.txt_yes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                    finish();
                    if (mAdViewDialog != null){
                        mAdViewDialog.destroy();
                    }

                }
            });
            mDialog = new Dialog(MainMenu.this, R.style.MaterialDialogSheet);
            mDialog.setContentView(view);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            if (mDialog.getWindow() != null) {
                mDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                mDialog.getWindow().setGravity(Gravity.CENTER);
            }
            mDialog.show();
        } catch (Exception ignored) {
        }
    }

//    private void ExitDialog() {
//        new AlertDialog.Builder(MainMenu.this)
//                .setTitle("Confirmation")
//                .setMessage("Do you want to exit?")
//                .setNeutralButton("Rate us", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        try {
//                            Intent intentMore = new Intent(Intent.ACTION_VIEW);
//                            intentMore.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName() + "&hl=en"));
//                            if (intentMore.resolveActivity(getPackageManager()) != null) {
//                                startActivity(intentMore);
//                            }
//                        } catch (Exception ignored) {
//                        }
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        finish();
//                    }
//                }).create().show();
//    }

    @Override
    public void onClick(View view) {

        viewId = view.getId();
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            onClickListner();
        }

    }

    public void onClickListner() {


        try {
            switch (viewId) {
                case R.id.iv_text_translation:
                    if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        startActivity(new Intent(this, Translate.class));
                    }
                    break;
                case R.id.iv_voice_conversation:
                    if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        startActivity(new Intent(MainMenu.this, ConversationActivity.class));
                    }
                    break;

            }
        } catch (Exception ignored) {
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
            mAdView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
