package com.megafreeapps.all.language.translator.free;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class Translate extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private static final int REQUEST_CODE = 1234;
    AdView mAdView;
    private TextToSpeech tts;
    private String alplhaText = "";
    private DB db;
    private Activity context;

    private Spinner from, to;
    private HashMap<String, String> langs;
    private String nativeText = "";
    private ArrayList<NameValuePair> params = new ArrayList<>(1);
    private Random rand = new Random();
    private EditText text;
    private TextView tvResult;
    private Button translate;
    private ClipboardManager myClipboard;

    private int viewId = 0;
    //    private AdView mAdView;
    private InterstitialAd mInterstitialAd;


    @Override
    public void onInit(int i)
    {

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translate_activity);
        context = Translate.this;
        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        tts = new TextToSpeech(context, this);
        myClipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.ad_int_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                    clickListener();
            }
        });
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        tvResult = findViewById(R.id.result);
        from = findViewById(R.id.fromSpin);
        from.getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        to = findViewById(R.id.toSpin);
        to.getBackground().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        text = findViewById(R.id.text);
        translate = findViewById(R.id.translate);
        ImageView voice = findViewById(R.id.voice);
        ImageView fromSpeak = findViewById(R.id.fromSpeak);
        ImageView toSpeak = findViewById(R.id.toSpeak);
        ImageView toCopy = findViewById(R.id.toCopy);
        ImageView toShare = findViewById(R.id.toShare);

        db = new DB(this);
        db.open();

        if (Connectivity.isConnected(context) && Connectivity.IsNetAvailable()) {
            new getIDText().execute();
        } else {
            nativeText = "class=\"t0\">";
            alplhaText = "class=\"o1\">";
        }


        fillData();

        from.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                text.setHint(from.getSelectedItem().toString());
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.lang_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        from.setAdapter(adapter);
        to.setAdapter(adapter);
        to.setSelection(0);
        from.setSelection(1);

        fromSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                speakOut(text.getText().toString().trim(), langs.get(from.getSelectedItem().toString()));
            }
        });

        toSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                speakOut(tvResult.getText().toString().trim(), langs.get(to.getSelectedItem().toString()));
            }
        });


        toCopy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myClipboard != null && !TextUtils.isEmpty(tvResult.getText().toString())) {
                    myClipboard.setPrimaryClip(ClipData.newPlainText("text", tvResult.getText().toString()));
                    Toast.makeText(v.getContext(), "Text Copied", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(v.getContext(), "Not Text Copy", Toast.LENGTH_SHORT).show();

                }
            }
        });

        toShare.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!TextUtils.isEmpty(tvResult.getText().toString())) {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", tvResult.getText().toString());
//                intent.addFlags(268435456);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(Intent.createChooser(intent, "Share Text"));
                    }
                }
                else {
                    Toast.makeText(v.getContext(), "Text not Appread", Toast.LENGTH_SHORT).show();
                }
            }
        });


        voice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                try {
                    if (getPackageManager().queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).size() != 0) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, langs.get(from.getSelectedItem().toString()));
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice Recognition Recording...");
                        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                        startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(Translate.this).create();
                        alertDialog.setTitle("Warning!");
                        alertDialog.setMessage("Voice Recognition Engine on Your Device is Not Active");
                        alertDialog.setButton("OK", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        alertDialog.show();
                    }
                } catch (Exception ignored) {
                }
            }
        });

        translate.setOnClickListener(this);
//        swap.setOnClickListener(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            text.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
            text.setSelection(text.length());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.translate, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.clear:
//                if (db != null)
//                {
//                    db.clear();
//                }

            case R.id.history:
                startActivity(new Intent(Translate.this, HistoryActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void fillData() {
        langs = new HashMap<>();
        langs.put("Afrikaans", "af");
        langs.put("Albanian", "sq");
        langs.put("Arabic", "ar");
        langs.put("Armenian", "hy");
        langs.put("Azerbaijani", "az");
        langs.put("Basque", "eu");
        langs.put("Bengali", "bn");
        langs.put("Belarusian", "be");
        langs.put("Bosnian", "bs");
        langs.put("Bulgarian", "bg");
        langs.put("Catalan", "ca");
        langs.put("Cebuano", "ceb");
        langs.put("Chinese Simplified", "zh-CN");
        langs.put("Chinese Traditional", "zh-TW");
        langs.put("Croatian", "hr");
        langs.put("Czech", "cs");
        langs.put("Danish", "da");
        langs.put("Dutch", "nl");
        langs.put("English", "en");
        langs.put("Esperanto", "eo");
        langs.put("Estonian", "et");
        langs.put("Filipino", "tl");
        langs.put("Finnish", "fi");
        langs.put("French", "fr");
        langs.put("Galician", "gl");
        langs.put("Georgian", "ka");
        langs.put("German", "de");
        langs.put("Greek", "el");
        langs.put("Gujarati", "gu");
        langs.put("Haitian Creole", "ht");
        langs.put("Hausa", "ha");
        langs.put("Hebrew", "iw");
        langs.put("Hindi", "hi");
        langs.put("Hmong", "hmn");
        langs.put("Hungarian", "hu");
        langs.put("Icelandic", "is");
        langs.put("Igbo", "ig");
        langs.put("Indonesian", "id");
        langs.put("Irish", "ga");
        langs.put("Italian", "it");
        langs.put("Japanese", "ja");
        langs.put("Javanese", "jw");
        langs.put("Kannada", "kn");
        langs.put("Khmer", "km");
        langs.put("Korean", "ko");
        langs.put("Lao", "lo");
        langs.put("Latin", "la");
        langs.put("Latvian", "lv");
        langs.put("Lithuanian", "lt");
        langs.put("Macedonian", "mk");
        langs.put("Malay", "ms");
        langs.put("Maltese", "mt");
        langs.put("Maori", "mi");
        langs.put("Marathi", "mr");
        langs.put("Mongolian", "mn");
        langs.put("Nepali", "ne");
        langs.put("Norwegian", "no");
        langs.put("Persian", "fa");
        langs.put("Polish", "pl");
        langs.put("Portuguese", "pt");
        langs.put("Punjabi", "pa");
        langs.put("Romanian", "ro");
        langs.put("Russian", "ru");
        langs.put("Serbian", "sr");
        langs.put("Slovak", "sk");
        langs.put("Slovenian", "sl");
        langs.put("Somali", "so");
        langs.put("Spanish", "es");
        langs.put("Swahili", "sw");
        langs.put("Swedish", "sv");
        langs.put("Tamil", "ta");
        langs.put("Telugu", "te");
        langs.put("Thai", "th");
        langs.put("Turkish", "tr");
        langs.put("Ukrainian", "uk");
        langs.put("Urdu", "ur");
        langs.put("Vietnamese", "vi");
        langs.put("Welsh", "cy");
        langs.put("Yiddish", "yi");
        langs.put("Yoruba", "yo");
        langs.put("Zulu", "zu");
        langs.put("Kazakh", "kk");
        langs.put("Chichewa", "ny");
        langs.put("Malagasy", "mg");
        langs.put("Malayalam", "ml");
        langs.put("Myanmar (Burnese)", "my");
        langs.put("Sesotho", "st");
        langs.put("Sundanese", "su");
        langs.put("Tajik", "tg");
        langs.put("Uzbek", "uz");
        langs.put("Sinhala", "si");
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

    @Override
    public void onClick(View v) {
        viewId = v.getId();
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            clickListener();
        }

    }

    private void clickListener() {
        switch (viewId) {
//            case R.id.swap:
//                int i = from.getSelectedItemPosition();
//                from.setSelection(to.getSelectedItemPosition(), true);
//                to.setSelection(i, true);
//                break;
            case R.id.translate:
                if (!TextUtils.isEmpty(text.getText().toString())) {
                    if (Connectivity.isConnected(context) && Connectivity.IsNetAvailable()) {
                        new JSONParse().execute();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), R.string.connection_faild, Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private void speakOut(final String text, final String language) {
        try {
            if (tts != null) {
                int result;
                switch (language) {
                    case "zh-CN":
                        result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                        break;
                    case "zh-TW":
                        result = tts.setLanguage(Locale.TRADITIONAL_CHINESE);
                        break;
                    case "fr":
                        result = tts.setLanguage(Locale.FRENCH);
                        break;
                    case "de":
                        result = tts.setLanguage(Locale.GERMAN);
                        break;
                    case "it":
                        result = tts.setLanguage(Locale.ITALIAN);
                        break;
                    case "ja":
                        result = tts.setLanguage(Locale.JAPANESE);
                        break;
                    case "ko":
                        result = tts.setLanguage(Locale.KOREAN);
                        break;
                    case "en":
                        result = tts.setLanguage(Locale.ENGLISH);
                        break;

                    default:
                        result = tts.setLanguage(new Locale(language));
                        break;
                }
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                    Log.e("TTS", "Language is not supported");
                    if (text.length() == 0) {
                        tts.speak("You haven't text", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                } else {
                    if (text.length() == 0) {
                        tts.speak("You haven't text", TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class JSONParse extends AsyncTask<Void, Void, Void> {
        String Result;
        String Source;

        protected void onPreExecute() {
            super.onPreExecute();
            translate.setVisibility(View.INVISIBLE);
        }

        protected Void doInBackground(Void... params) {
            String[] split = new parser().translate(text.getText().toString(), langs.get(to.getSelectedItem().toString()),
                    langs.get(from.getSelectedItem().toString()), nativeText, alplhaText).split("\\+");
//            Log.i("spliting", split[0] + " " + split[1]);
            if (split.length > 0) {
                Source = split[0];
            } else {
                Source = "";
            }
            if (split.length > 1) {
                Result = split[1];
            } else {
                Result = "";
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            translate.setVisibility(View.VISIBLE);
            String result1 = "";
            String result2 = "";
//            String result3 = "";
            try {
                result1 = Source;
//                result3 = "\n";
                result2 = Result.replace("&#39;", "'").replace("d>", result1);
                Log.i("source", Source);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (db != null) {
                    db.insertResult(rand.nextInt(), from.getSelectedItem().toString(), to.getSelectedItem().toString(), text.getText().toString(), result1, langs.get(from.getSelectedItem().toString()), langs.get(to.getSelectedItem().toString()), result2);
                }
                tvResult.setText(result1);
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getIDText extends AsyncTask<Void, String, JSONObject> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected JSONObject doInBackground(Void... args) {
            String x = "https://dl.dropboxusercontent.com/s/0l6wxpuezi5li30/dict.json";
//            Log.i("url", x);
            //            Log.d("", json.toString());
            return new JSONParser().getJSONFromUrl(x, params);
        }

        protected void onPostExecute(JSONObject json) {
            try {
                if (json != null) {
                    nativeText = json.getString("lang1");
                    alplhaText = json.getString("lang2");
                    Log.i("nativeText", nativeText);
                    Log.i("alplhaText", alplhaText);
                } else {
                    nativeText = "class=\"t0\">";
                    alplhaText = "class=\"o1\">";
                }
            } catch (JSONException e) {
                nativeText = "class=\"t0\">";
                alplhaText = "class=\"o1\">";
                e.printStackTrace();
            }
        }
    }

}
