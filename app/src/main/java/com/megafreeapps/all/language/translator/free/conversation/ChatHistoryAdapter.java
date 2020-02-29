package com.megafreeapps.all.language.translator.free.conversation;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.megafreeapps.all.language.translator.free.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ChatHistoryAdapter extends BaseAdapter implements TextToSpeech.OnInitListener
{
    private Activity context;
    private TextToSpeech tts;
    private ClipboardManager myClipboard;
    //    private View fromPro1;
    private LayoutInflater inflater;
    //    private SparseBooleanArray mSelectedItemsIds;
    //    private View nativePro1;
    private ArrayList<ChatHistoryRow> chat_result;

    ChatHistoryAdapter(Activity context, ArrayList<ChatHistoryRow> chat_result)
    {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.chat_result = chat_result;
        tts = new TextToSpeech(context, this);
        myClipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
//        mSelectedItemsIds = new SparseBooleanArray();
    }

    public int getCount()
    {
        return chat_result.size();
    }

//    private void selectView(int position, boolean value)
//    {
//        if (value)
//        {
//            mSelectedItemsIds.put(position, true);
//        }
//        else
//        {
//            mSelectedItemsIds.delete(position);
//        }
//        notifyDataSetChanged();
//    }

//    public int getSelectedCount()
//    {
//        return mSelectedItemsIds.size();
//    }
//
//    public SparseBooleanArray getSelectedIds()
//    {
//        return mSelectedItemsIds;
//    }
//
//    public void toggleSelection(int position)
//    {
//        selectView(position, !mSelectedItemsIds.get(position));
//    }
//
//    public void remove(ResultRow object)
//    {
//        result.remove(object);
//        notifyDataSetChanged();
//    }
//
//    public void removeSelection()
//    {
//        mSelectedItemsIds = new SparseBooleanArray();
//        notifyDataSetChanged();
//    }

    public Object getItem(int arg0)
    {
        return chat_result.get(arg0);
    }

    public long getItemId(int arg0)
    {
        return (long) arg0;
    }

    public View getView(final int arg0, View arg1, ViewGroup arg2)
    {
        View rowView = inflater.inflate(R.layout.chat_history_row, null);
        try
        {
            final TextView fromText = rowView.findViewById(R.id.chat_fromText);
            TextView to = rowView.findViewById(R.id.chat_to);
            final TextView toText = rowView.findViewById(R.id.chat_toText);
            ((TextView) rowView.findViewById(R.id.chat_from)).setText(chat_result.get(arg0).from);
            to.setText(chat_result.get(arg0).to);
            fromText.setText(chat_result.get(arg0).fromText);
            toText.setText(chat_result.get(arg0).toText);

            rowView.findViewById(R.id.chat_fromCopy).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    myClipboard.setPrimaryClip(ClipData.newPlainText("text", chat_result.get(arg0).fromText));
                    Toast.makeText(v.getContext(), "Copy Source", Toast.LENGTH_SHORT).show();
                }
            });
            rowView.findViewById(R.id.chat_toCopy).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    myClipboard.setPrimaryClip(ClipData.newPlainText("text", chat_result.get(arg0).toText));
                    Toast.makeText(v.getContext(), "Copy Native", Toast.LENGTH_SHORT).show();
                }
            });

            rowView.findViewById(R.id.chat_fromShare).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", chat_result.get(arg0).fromText);
//                intent.addFlags(268435456);
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                    {
                        context.startActivity(Intent.createChooser(intent, "Share Text"));
                    }
                }
            });
            rowView.findViewById(R.id.chat_toShare).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View arg2)
                {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", chat_result.get(arg0).toText);
//                intent.addFlags(PendingIntent.FLAG_CANCEL_CURRENT);
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                    {
                        context.startActivity(Intent.createChooser(intent, "Share Text"));
                    }
                }
            });

            rowView.findViewById(R.id.chat_toSpeak).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View arg2)
                {
                    speakOut(toText.getText().toString().trim(), chat_result.get(arg0).toCode);
                }
            });
            rowView.findViewById(R.id.chat_fromSpeak).setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View arg2)
                {
                    speakOut(fromText.getText().toString().trim(), chat_result.get(arg0).fromCode);
                }
            });
        }
        catch (Exception ignored)
        {
        }
        return rowView;
    }

    private void speakOut(final String text, final String language)
    {
        try
        {
            if (tts != null)
            {
                int result;
                switch (language)
                {
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
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                {
                    tts.setLanguage(Locale.US);
                    Log.e("TTS", "Language is not supported");
                    if (text.length() == 0)
                    {
                        tts.speak("You haven't text", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else
                    {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                else
                {
                    if (text.length() == 0)
                    {
                        tts.speak("You haven't text", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else
                    {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    @Override
    public void onInit(int i)
    {

    }
}
