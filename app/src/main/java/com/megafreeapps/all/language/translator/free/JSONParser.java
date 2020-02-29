package com.megafreeapps.all.language.translator.free;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/* compiled from: Translate */
class JSONParser
{
    private static InputStream is = null;
    private static JSONObject jObj = null;
    private static String json = "";

    JSONObject getJSONFromUrl(String url, ArrayList<NameValuePair> params)
    {
        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            is = httpClient.execute(httpPost).getEntity().getContent();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (ClientProtocolException e2)
        {
            e2.printStackTrace();
        }
        catch (IOException e3)
        {
            e3.printStackTrace();
        }
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            while (true)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                sb.append(String.valueOf(line)).append("\n");
            }
            is.close();
            json = sb.toString();
        }
        catch (Exception e4)
        {
            Log.e("Buffer Error", "Error converting result " + e4.toString());
        }
        try
        {
            jObj = new JSONObject(json);
        }
        catch (JSONException e5)
        {
            Log.e("JSON Parser", "Error parsing data " + e5.toString());
        }
        return jObj;
    }
}
