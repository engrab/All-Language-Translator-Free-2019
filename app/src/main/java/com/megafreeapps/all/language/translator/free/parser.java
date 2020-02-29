package com.megafreeapps.all.language.translator.free;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class parser
{
    private String getText(String url) throws Exception
    {
        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        while (true)
        {
            String inputLine = in.readLine();
            if (inputLine == null)
            {
                in.close();
                return response.toString();
            }
            response.append(inputLine);
        }
    }

    String translate(String to_translate, String to_langage, String from_langage, String nativeText, String alplhaText)
    {
        String charset = "UTF-8";
        try
        {
            String hl = URLEncoder.encode(to_langage, charset);
            String sl = URLEncoder.encode(from_langage, charset);
            String q = URLEncoder.encode(to_translate, charset);
            try
            {
                String page = getText(String.format("https://translate.google.com/m?sl=%s&tl=%s&q=%s", sl, hl, q));
                String result = page.substring(page.indexOf(nativeText) + nativeText.length()).split("<")[0];
                return result + "+" + page.substring(page.indexOf(alplhaText) + alplhaText.length()).split("<")[0];
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return e.toString();
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            return "1";
        }
    }
}
