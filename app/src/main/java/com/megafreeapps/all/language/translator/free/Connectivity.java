package com.megafreeapps.all.language.translator.free;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Connectivity
{
    private static int Result;

    public Connectivity()
    {
    }

    private static NetworkInfo getNetworkInfo(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null)
        {
            return cm.getActiveNetworkInfo();
        }
        return null;
    }

    static boolean isConnected(Context context)
    {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnected();
    }

    //************************** Internet Available *************************
    static boolean IsNetAvailable()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Runtime runtime = Runtime.getRuntime();
                    Process process = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                    Result = process.waitFor();
                }
                catch (Exception e)
                {
//					Toast.makeText(context, "Exp \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
        return (Result == 0);
    }

}