package com.megafreeapps.all.language.translator.free.conversation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.megafreeapps.all.language.translator.free.R;

import java.util.ArrayList;

public class ChatHistoryActivity extends AppCompatActivity {
    AdView mAdView;
    private ArrayList<ChatHistoryRow> chat_results;
    private ChatDataBase db;
    private ListView resultList;
    ChatHistoryAdapter resultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });

        resultList = findViewById(R.id.resultList);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chat_results != null && chat_results.size() > 0) {
                    new AlertDialog.Builder(ChatHistoryActivity.this)
                            .setTitle(R.string.action_delete_title)
                            .setMessage(R.string.action_delete_msg)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    chat_results.clear();
                                    if (db != null) {
                                        db.clear();
                                    }


                                    if (resultList != null) {
                                        resultList.setAdapter(null);
                                    }
                                }
                            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }).setIcon(R.drawable.ic_delete_black_24dp).show();
                } else {
                    Toast.makeText(ChatHistoryActivity.this, "History Aleardy Empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        db = new ChatDataBase(this);
        db.open();
        chat_results = new ArrayList<>();
        chat_results = db.getResults();
        if (chat_results != null && chat_results.size() > 0) {
            resultAdapter = new ChatHistoryAdapter(this, chat_results);
            resultList.setAdapter(resultAdapter);
        }

        resultList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, final int pos, long id) {
                if (chat_results != null && chat_results.size() > 0) {
                    new AlertDialog.Builder(ChatHistoryActivity.this).setTitle(R.string.action_delete_title).setMessage(R.string.action_delete_msg).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            if (chat_results != null && chat_results.size() > 0) {
                                if (db != null) {
                                    db.deleteResult(chat_results.get(pos).id);
                                }
                                chat_results.remove(pos);
                            }
                            if (resultAdapter != null) {
                                resultAdapter.notifyDataSetChanged();
                            }
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }).setIcon(R.drawable.ic_delete_black_24dp).show();
                }
                return true;
            }
        });

        
    }

    public void onDestroy() {

        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    public void onBackPressed() {

            super.onBackPressed();

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
        }
    }


}
