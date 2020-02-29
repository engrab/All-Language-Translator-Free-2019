package com.megafreeapps.all.language.translator.free.conversation;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.megafreeapps.all.language.translator.free.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/*
        Class : Defines Custom Array Adapter Class : Chat Array Adapter
                Modified to from list view of objects of type ChatMessage
 */
class ChatArrayAdapter extends ArrayAdapter<ChatMessage> implements TextToSpeech.OnInitListener {

    private ArrayList<ChatMessage> mListChatMessages = new ArrayList<>();
    HashMap<String, String> map = new HashMap<>();
    ClipboardManager myClipboard;
    Context context;
    private TextToSpeech tts;

    @Override
    public void add(ChatMessage object) {
        super.add(object);
        mListChatMessages.add(object);
    }

    @Override
    public void clear() {
        super.clear();
        mListChatMessages.clear();
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context=context;
        myClipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        tts = new TextToSpeech(context, this);
    }

    public int getCount() {
        return this.mListChatMessages.size();
    }

    public ChatMessage getItem(int index) {
        return this.mListChatMessages.get(index);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final ChatMessage chatMessage = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessage != null) {
            if (chatMessage.getmLeft()) {
                row = inflater.inflate(R.layout.chat_translate_left, parent, false);
            } else {
                row = inflater.inflate(R.layout.chat_translate_right, parent, false);
            }
//
//                if (chatMessage.getmLeft()) {
//                    row = inflater.inflate(R.layout.chat_left, parent, false);
//                } else {
//                    row = inflater.inflate(R.layout.chat_right, parent, false);
//                }

            final TextView TextMessage = row.findViewById(R.id.message);
            TextMessage.setText(chatMessage.getmMessage());

            final TextView TransMessage = row.findViewById(R.id.targetTrans);
            TransMessage.setText(chatMessage.getmTranslate());



            row.findViewById(R.id.iv_copy).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    myClipboard.setPrimaryClip(ClipData.newPlainText("text", chatMessage.getmTranslate()));
                    Toast.makeText(v.getContext(), "Copy Source", Toast.LENGTH_SHORT).show();
                }
            });

            row.findViewById(R.id.iv_share).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, chatMessage.getmTranslate());
//                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                    {
                        context.startActivity(Intent.createChooser(intent, "Share via"));
                    }

//                    context.startActivity(Intent.createChooser(intent, "Share Text"));
                }
            });

            row.findViewById(R.id.iv_play).setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg2) {
                    ChatMessage chatMessage = mListChatMessages.get(position);
                    if (chatMessage != null) {
                        speakOut(chatMessage.getmTranslate(), chatMessage.getmLanguageCode());
                    }
                }
            });

        }
        return row;
    }

    //  TEXT TO SPEECH ACTION
    @SuppressWarnings("deprecation")
    private void speakOut(String textMessage, String languageCode) {
        int result = tts.setLanguage(new Locale(languageCode));
        Log.e("Inside", "speakOut " + languageCode + " " + result);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            Toast.makeText(context, "Install Missing Language", Toast.LENGTH_SHORT).show();
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            context.startActivity(installIntent);
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(context, "This Language is not supported", Toast.LENGTH_SHORT).show();
        } else {

            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
            tts.speak(textMessage, TextToSpeech.QUEUE_FLUSH, map);

        }
    }


    @Override
    public void onInit(int status) {

    }
}