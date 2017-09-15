package jatx.asta_app;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jatx on 11.09.17.
 */

public class ImgDownloadIntentService extends IntentService {
    public static final String url =
            "http://jl-workshop.com/Cloud-Storage-Direct-Link-Generator/api-1.0/dlg?download&url=https://yadi.sk/d/xvJflNsq3MokRG";

    public static final String INTENT_DOWNLOAD_FINISHED = "jatx.asta_app.DOWNLOAD_FINISHED";
    public static final String INTENT_UPDATE_PROGRESS = "jatx.asta_app.UPDATE_PROGRESS";

    public ImgDownloadIntentService() {
        super("ImgDownloadIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String imgSavePath = intent.getStringExtra("imgSavePath");

        try {
            boolean downloadOk = false;

            while (!downloadOk) {
                URL obj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setReadTimeout(5000);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                conn.addRequestProperty("Referer", "google.com");

                System.out.println("Request URL ... " + url);

                boolean redirect = false;

                // normally, 3xx is redirect
                int status = conn.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER)
                        redirect = true;
                }

                System.out.println("Response Code ... " + status);

                if (redirect) {

                    // get redirect url from "location" header field
                    String newUrl = conn.getHeaderField("Location");

                    // get the cookie if need, for login
                    String cookies = conn.getHeaderField("Set-Cookie");

                    // open the new connnection again
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setRequestProperty("Cookie", cookies);
                    conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                    conn.addRequestProperty("User-Agent", "Mozilla");
                    conn.addRequestProperty("Referer", "google.com");

                    System.out.println("Redirect to URL : " + newUrl);

                }

                InputStream is = conn.getInputStream();
                OutputStream os = new FileOutputStream(imgSavePath);

                long prevBytesTotal = 0;
                long bytesTotal = 0;
                byte[] buffer = new byte[20480];

                int bytesRead = 0;
                while (bytesRead >= 0) {
                    bytesRead = is.read(buffer);
                    if (bytesRead > 0) {
                        os.write(buffer, 0, bytesRead);
                        bytesTotal += bytesRead;
                    }
                    if (bytesTotal - prevBytesTotal > 1024 * 1024) {
                        prevBytesTotal = bytesTotal;
                        int mbTotal = (int) (bytesTotal / (1024 * 1024));
                        Intent intent2 = new Intent(INTENT_UPDATE_PROGRESS);
                        intent2.putExtra("mbProgress", mbTotal);
                        sendBroadcast(intent2);
                    }
                }

                os.close();
                is.close();

                downloadOk = true;
            }

            Intent intent1 = new Intent(INTENT_DOWNLOAD_FINISHED);
            intent1.putExtra("result", "success");
            sendBroadcast(intent1);
        } catch (Throwable e) {
            e.printStackTrace();

            Intent intent1 = new Intent(INTENT_DOWNLOAD_FINISHED);
            intent1.putExtra("result", "error");
            sendBroadcast(intent1);
        }
    }
}
