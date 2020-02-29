package com.megafreeapps.all.language.translator.free;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ResultAdapter extends BaseAdapter implements TextToSpeech.OnInitListener
{
    private Activity context;
    private TextToSpeech tts;
    private ClipboardManager myClipboard;
    //    private View fromPro1;
    private LayoutInflater inflater;
    //    private SparseBooleanArray mSelectedItemsIds;
    //    private View nativePro1;
    private ArrayList<ResultRow> result;

    ResultAdapter(Activity context, ArrayList<ResultRow> result)
    {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.result = result;
        tts = new TextToSpeech(context, this);
        myClipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
//        mSelectedItemsIds = new SparseBooleanArray();
    }

    public int getCount()
    {
        return result.size();
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
        return result.get(arg0);
    }

    public long getItemId(int arg0)
    {
        return (long) arg0;
    }

    public View getView(final int arg0, View arg1, ViewGroup arg2)
    {
        View rowView = inflater.inflate(R.layout.result_row, null);
        try
        {
            final TextView fromText = rowView.findViewById(R.id.fromText);
            TextView to = rowView.findViewById(R.id.to);
            final TextView toText = rowView.findViewById(R.id.toText);
            TextView nativeT = rowView.findViewById(R.id.nativeT);
            final TextView nativeText = rowView.findViewById(R.id.nativeText);
            ((TextView) rowView.findViewById(R.id.from)).setText(result.get(arg0).from);
            to.setText(result.get(arg0).to);
            fromText.setText(result.get(arg0).fromText);
            toText.setText(result.get(arg0).toText);
            nativeT.setText(result.get(arg0).to + " - Alphabet");
            nativeText.setText(result.get(arg0).nativeText);
            rowView.findViewById(R.id.fromCopy).setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    myClipboard.setPrimaryClip(ClipData.newPlainText("text", result.get(arg0).fromText));
                    Toast.makeText(v.getContext(), "Copy Source", Toast.LENGTH_SHORT).show();
                }
            });
            rowView.findViewById(R.id.toCopy).setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    myClipboard.setPrimaryClip(ClipData.newPlainText("text", result.get(arg0).toText));
                    Toast.makeText(v.getContext(), "Copy Native", Toast.LENGTH_SHORT).show();
                }
            });
            rowView.findViewById(R.id.nativeCopy).setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    myClipboard.setPrimaryClip(ClipData.newPlainText("text", result.get(arg0).nativeText));
                    Toast.makeText(v.getContext(), "Copy Text", Toast.LENGTH_SHORT).show();
                }
            });
            rowView.findViewById(R.id.fromShare).setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", result.get(arg0).fromText);
//                intent.addFlags(268435456);
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                    {
                        context.startActivity(Intent.createChooser(intent, "Share Text"));
                    }
                }
            });
            rowView.findViewById(R.id.toShare).setOnClickListener(new OnClickListener()
            {
                public void onClick(View arg2)
                {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", result.get(arg0).toText);
//                intent.addFlags(PendingIntent.FLAG_CANCEL_CURRENT);
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                    {
                        context.startActivity(Intent.createChooser(intent, "Share Text"));
                    }
                }
            });
            rowView.findViewById(R.id.nativeShare).setOnClickListener(new OnClickListener()
            {
                public void onClick(View arg2)
                {
                    Intent intent = new Intent("android.intent.action.SEND");
                    intent.setType("text/plain");
                    intent.putExtra("android.intent.extra.TEXT", result.get(arg0).nativeText);
//                intent.addFlags(268435456);
                    if (intent.resolveActivity(context.getPackageManager()) != null)
                    {
                        context.startActivity(Intent.createChooser(intent, "Share Text"));
                    }
                }
            });
            rowView.findViewById(R.id.nativeSpeak).setOnClickListener(new OnClickListener()
            {
                public void onClick(View arg2)
                {
                    speakOut(nativeText.getText().toString().trim(), result.get(arg0).toCode);
                }
            });
            rowView.findViewById(R.id.toSpeak).setOnClickListener(new OnClickListener()
            {
                public void onClick(View arg2)
                {
                    speakOut(toText.getText().toString().trim(), result.get(arg0).toCode);
                }
            });
            rowView.findViewById(R.id.fromSpeak).setOnClickListener(new OnClickListener()
            {
                public void onClick(View arg2)
                {
                    speakOut(fromText.getText().toString().trim(), result.get(arg0).fromCode);
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
