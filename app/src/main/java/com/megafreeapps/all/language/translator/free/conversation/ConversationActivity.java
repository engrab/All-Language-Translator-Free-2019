package com.megafreeapps.all.language.translator.free.conversation;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.megafreeapps.all.language.translator.free.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import static com.megafreeapps.all.language.translator.free.conversation.GlobalVars.BASE_REQ_URL;
import static com.megafreeapps.all.language.translator.free.conversation.GlobalVars.DEFAULT_LANG_POS;
import static com.megafreeapps.all.language.translator.free.conversation.GlobalVars.DEFAULT_LANG_POS2;
import static com.megafreeapps.all.language.translator.free.conversation.GlobalVars.LANGUAGE_CODES;


public class ConversationActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static final String LOG_TAG = ConversationActivity.class.getName();
    private static final int REQ_CODE_SPEECH_INPUT_FROM = 1;
    private static final int REQ_CODE_SPEECH_INPUT_TO = 2;
    int number_of_ads = 0;
    AdView mAdView;
    int mPosition = 0;
    private ClipboardManager myClipboard;
    private ChatDataBase db;
    private Random rand = new Random();
    private HashMap<String, String> langs;
    private InterstitialAd mInterstitialAd;


    private TextToSpeech mTextToSpeech;                                     //    Text to Speech Engine
    private Spinner mSpinnerLanguageFrom;                                   //    Dropdown list for selecting base language (From)
    private Spinner mSpinnerLanguageTo;                                     //    Dropdown list for selecting base language (To)
    private String mLanguageCodeFrom = "en";                                //    Language Code (From)
    private String mLanguageCodeTo = "en";                                  //    Language Code (To)
    private ChatArrayAdapter chatArrayAdapter;                              //    Custom Array Adapter class object
    private ListView mListView;                                             //    Listview to show chat
    private LinearLayout mLinearLayoutKeyboardPopup;
    TextView txt_from, txt_to;
    RelativeLayout ll_bottom;
    private String mChatInput;
    private String mChatOutPut;
    private EditText mEditTextChatKeyboardInput;
    private boolean mLeftSide;
    private Dialog process_tts;                                             //    Dialog box for Text to Speech Engine Language Switch
    HashMap<String, String> map = new HashMap<>();
//    volatile boolean activityRunning;      //    To track status of current activity

    private ProgressDialog progressDialog;
    Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

//        setProgressDialog();

//        activityRunning = true;
        mSpinnerLanguageFrom = findViewById(R.id.spinner_language_from);
        mSpinnerLanguageTo = findViewById(R.id.spinner_language_to);
        ImageView mImageKeyboardFrom = findViewById(R.id.image_keyboard_from);      //      Input Keyboard button (From)
        ImageView mImageKeyboardTo = findViewById(R.id.image_keyboard_to);          //      Input Keyboard button (To)
        ImageView mImageMicFrom = findViewById(R.id.image_mic_from);                //      Input Mic button (From)
        ImageView mImageMicTo = findViewById(R.id.image_mic_to);                    //      Input Mic button (To)
        TextView mEmptyTextView = findViewById(R.id.empty_view_not_connected);
        mListView = findViewById(R.id.list_chat_view);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_translate_left);
        mLinearLayoutKeyboardPopup = findViewById(R.id.popup_keyboard);
        ll_bottom = findViewById(R.id.ll_bottom);
        mEditTextChatKeyboardInput = findViewById(R.id.text_keyboard_input);
        process_tts = new Dialog(ConversationActivity.this);
        process_tts.setContentView(R.layout.dialog_processing_tts);
        process_tts.setTitle(getString(R.string.process_tts));
        TextView title = process_tts.findViewById(android.R.id.title);
        // title.setSingleLine(false);
        mTextToSpeech = new TextToSpeech(this, this);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        txt_from = findViewById(R.id.txt_from);
        txt_to = findViewById(R.id.txt_to);

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
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        fillData();
        db = new ChatDataBase(this);
        db.open();
        //  CHECK INTERNET CONNECTION
        if (!isOnline()) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);

            //  GET LANGUAGES LIST
            new GetLanguages().execute();
            mListView.setAdapter(chatArrayAdapter);

            //  MIC BUTTON ACTION : SPEECH TO TEXT
            mImageMicFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLanguageCodeFrom);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
                    try {
                        number_of_ads++;
                        if (number_of_ads % 3 == 0) {
                            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            }
                        }
                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_FROM);

                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getApplicationContext(), getString(R.string.language_not_supported), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mImageMicTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLanguageCodeTo);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
                    try {
                        number_of_ads++;

                        if (number_of_ads % 3 == 0) {
                            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            }
                        }
                        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_TO);
                    } catch (ActivityNotFoundException a) {
                        Toast.makeText(getApplicationContext(), getString(R.string.language_not_supported), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //  KEYBOARD CLICK ACTION
            mImageKeyboardFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLinearLayoutKeyboardPopup.setVisibility(View.VISIBLE);
                    ll_bottom.setVisibility(View.GONE);
                    ImageView imageViewSend = findViewById(R.id.image_send);
                    ImageView imageViewBack = findViewById(R.id.image_back);
                    mEditTextChatKeyboardInput.setText("");
                    mEditTextChatKeyboardInput.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditTextChatKeyboardInput, InputMethodManager.SHOW_IMPLICIT);
                    imageViewSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(), 0);
                            mLinearLayoutKeyboardPopup.setVisibility(View.VISIBLE);
                            mChatInput = mEditTextChatKeyboardInput.getText().toString();
                            mLeftSide = true;
                            new TranslateText().execute(mChatInput);
                            mEditTextChatKeyboardInput.setText("");

                        }
                    });
                    imageViewBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(), 0);
                            ll_bottom.setVisibility(View.VISIBLE);
                            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
                        }
                    });
                }
            });
            mImageKeyboardTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ll_bottom.setVisibility(View.GONE);
                    mLinearLayoutKeyboardPopup.setVisibility(View.VISIBLE);
                    ImageView imageViewSend = findViewById(R.id.image_send);
                    ImageView imageViewBack = findViewById(R.id.image_back);
                    mEditTextChatKeyboardInput.setText("");
                    mEditTextChatKeyboardInput.requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditTextChatKeyboardInput, InputMethodManager.SHOW_IMPLICIT);
                    imageViewSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(), 0);
                            mLinearLayoutKeyboardPopup.setVisibility(View.VISIBLE);
                            mLeftSide = false;
                            mChatInput = mEditTextChatKeyboardInput.getText().toString();
                            new TranslateText().execute(mChatInput);
                            mEditTextChatKeyboardInput.setText("");
                        }
                    });
                    imageViewBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm.hideSoftInputFromWindow(mEditTextChatKeyboardInput.getWindowToken(), 0);
                            mLinearLayoutKeyboardPopup.setVisibility(View.GONE);
                            ll_bottom.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
            //  SPINNER LANGUAGE FROM
            mSpinnerLanguageFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeFrom = LANGUAGE_CODES.get(position);
                    txt_from.setText(mLanguageCodeFrom);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });
            //  SPINNER LANGUAGE TO
            mSpinnerLanguageTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mLanguageCodeTo = LANGUAGE_CODES.get(position);
                    txt_to.setText(mLanguageCodeTo);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();
                }
            });
            mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListView.setAdapter(chatArrayAdapter);
            //to scroll the list view to bottom on data change
            chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    mListView.setSelection(chatArrayAdapter.getCount() - 1);
                }
            });
            //  TEXT TO SPEECH
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                }
            });


            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                    return false;


                }
            });


        }
    }


    //  CHECK INTERNET CONNECTION
    public boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return false;
    }

    //  RESULT OF SPEECH INPUT
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT_FROM: {
                if (resultCode == RESULT_OK && null != data) {
                    /*
                            Dialog box to show list of processed Speech to text results
                            User selects matching text to display in chat
                     */
                    final Dialog match_text_dialog = new Dialog(ConversationActivity.this);
                    match_text_dialog.setContentView(R.layout.dialog_matches_frag);
                    match_text_dialog.setTitle(getString(R.string.select_matching_text));
                    ListView textlist = match_text_dialog.findViewById(R.id.list);
                    final ArrayList<String> matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matches_text);
                    textlist.setAdapter(adapter);
                    textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mChatInput = matches_text.get(position);
                            mLeftSide = true;
                            match_text_dialog.dismiss();
                            new TranslateText().execute(mChatInput);

                        }
                    });
                    match_text_dialog.show();

                    break;
                }
            }
            case REQ_CODE_SPEECH_INPUT_TO: {
                if (resultCode == RESULT_OK && null != data) {
                    /*
                            Dialog box to show list of processed Speech to text results
                            User selects matching text to display in chat
                     */
                    final Dialog match_text_dialog = new Dialog(ConversationActivity.this);
                    match_text_dialog.setContentView(R.layout.dialog_matches_frag);
                    match_text_dialog.setTitle(getString(R.string.select_matching_text));
                    ListView textlist = match_text_dialog.findViewById(R.id.list);
                    final ArrayList<String> matches_text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, matches_text);
                    textlist.setAdapter(adapter);
                    textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mChatInput = matches_text.get(position);
                            mChatOutPut = mChatInput;
                            mLeftSide = false;
                            match_text_dialog.dismiss();
                            new TranslateText().execute(mChatInput);

                        }
                    });
                    match_text_dialog.show();

                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_menu_clear, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                chatArrayAdapter.clear();
                chatArrayAdapter.notifyDataSetChanged();
                break;
            case R.id.history:
                startActivity(new Intent(ConversationActivity.this, ChatHistoryActivity.class));
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            default:
                break;
        }
        return true;
    }

    //  INITIALISE TEXT TO SPEECH ENGINE
    @Override
    public void onInit(int status) {
        Log.e("Inside----->", "onInit");
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(new Locale("en"));
            if (result == TextToSpeech.LANG_MISSING_DATA) {
                Toast.makeText(getApplicationContext(), getString(R.string.language_pack_missing), Toast.LENGTH_SHORT).show();
            } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.language_not_supported), Toast.LENGTH_SHORT).show();
            }
            mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.e("Inside", "OnStart");
                    process_tts.hide();
                }

                @Override
                public void onDone(String utteranceId) {
                }

                @Override
                public void onError(String utteranceId) {

                }
            });
        } else {
            Log.e(LOG_TAG, "TTS Initilization Failed");
        }
    }

    //  TEXT TO SPEECH ACTION
    @SuppressWarnings("deprecation")
    private void speakOut(String textMessage, String languageCode) {
        int result = mTextToSpeech.setLanguage(new Locale(languageCode));
        Log.e("Inside", "speakOut " + languageCode + " " + result);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            Toast.makeText(getApplicationContext(), getString(R.string.language_pack_missing), Toast.LENGTH_SHORT).show();
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(getApplicationContext(), getString(R.string.language_not_supported), Toast.LENGTH_SHORT).show();
        } else {
            process_tts.show();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            mTextToSpeech.speak(textMessage, TextToSpeech.QUEUE_FLUSH, map);
            process_tts.dismiss();
        }
    }

    //  WHEN ACTIVITY IS PAUSED
    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
        super.onPause();
    }

    //  WHEN ACTIVITY IS DESTROYED
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
//        activityRunning = false;

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    private void setProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }


    //  NEW CHAT MESSAGE
    private boolean sendChatMessage(String textTranslated) {
        Log.e(LOG_TAG, "New chat ---------> " + mChatInput + " " + textTranslated);
        String from, to;
        if (mLeftSide) {
            from = mLanguageCodeFrom;
            to = mLanguageCodeTo;
        } else {
            from = mLanguageCodeTo;
            to = mLanguageCodeFrom;
        }
        chatArrayAdapter.add(new ChatMessage(mLeftSide, textTranslated, mChatInput, to));
//        chatArrayAdapter.add(new ChatMessage(mLeftSide, mChatInput, textTranslated, to));

        ChatMessage chatMessage = chatArrayAdapter.getItem(chatArrayAdapter.getCount() - 1);
        if (chatMessage != null) {
            speakOut(chatMessage.getmTranslate(), chatMessage.getmLanguageCode());
            if (db != null) {
                db.insertResult(rand.nextInt(), mSpinnerLanguageFrom.getSelectedItem().toString(), mSpinnerLanguageTo.getSelectedItem().toString(), mChatInput, textTranslated, from, to);
//                Toast.makeText(getApplicationContext(), "item set in db", Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    //  SUBCLASS TO TRANSLATE TEXT ON BACKGROUND THREAD
    private class TranslateText extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... input) {
            if (input[0].isEmpty()) {
                return "";
            } else {
                String from, to;
                if (mLeftSide) {
                    from = mLanguageCodeFrom;
                    to = mLanguageCodeTo;
                } else {
                    from = mLanguageCodeTo;
                    to = mLanguageCodeFrom;
                }
                Uri baseUri = Uri.parse(BASE_REQ_URL);
                Uri.Builder uriBuilder = baseUri.buildUpon();
                uriBuilder.appendPath("translate")
                        .appendQueryParameter("key", getString(R.string.API_KEY))
                        .appendQueryParameter("lang", from + "-" + to)
                        .appendQueryParameter("text", input[0]);
                Log.e("String Url ---->", uriBuilder.toString());
                return QueryUtils.fetchTranslation(uriBuilder.toString());
            }
        }

        @Override
        protected void onPostExecute(String result) {
//            if (activityRunning)
            sendChatMessage(result);
        }
    }

    //  SUBCLASS TO GET LIST OF LANGUAGES ON BACKGROUND THREAD
    private class GetLanguages extends AsyncTask<Void, Void, ArrayList<String>> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ConversationActivity.this);
            progressDialog.setTitle("Hi");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {


            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("getLangs")
                    .appendQueryParameter("key", getString(R.string.API_KEY))
                    .appendQueryParameter("ui", "en");
            Log.e("String Url ---->", uriBuilder.toString());
            return QueryUtils.fetchLanguages(uriBuilder.toString());
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
//            if (activityRunning) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ConversationActivity.this, android.R.layout.simple_spinner_item, result);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (result.size() > 0) {
                mSpinnerLanguageFrom.setAdapter(adapter);
                mSpinnerLanguageTo.setAdapter(adapter);
                //  SET DEFAULT LANGUAGE SELECTIONS
                mSpinnerLanguageFrom.setSelection(DEFAULT_LANG_POS);
                mSpinnerLanguageTo.setSelection(DEFAULT_LANG_POS2);
            }
            progressDialog.dismiss();
//            }
        }
    }
}
