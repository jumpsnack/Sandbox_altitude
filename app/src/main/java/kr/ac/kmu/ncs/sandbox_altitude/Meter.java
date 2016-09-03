package kr.ac.kmu.ncs.sandbox_altitude;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by NCS-KSW on 2016-09-03.
 */
public class Meter extends AsyncTask<Number, Void, Float> {

    private static final String WS_URL = "http://ws.geonames.org/findNearByWeatherJSON";
    private static final String SLP_STRING = "slp";

    @Override
    protected Float doInBackground(Number... numbers) {
        Float mslp = null;
        HttpURLConnection urlConnection = null;

        try {
            Uri uri = Uri.parse(WS_URL)
                    .buildUpon()
                    .appendQueryParameter("lat", String.valueOf(numbers[0]))
                    .appendQueryParameter("lng", String.valueOf(numbers[1]))
                    .appendQueryParameter("username", "demo")
                    .build();

            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection)url.openConnection();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            Scanner inputStreamScanner = new Scanner(inputStream).useDelimiter("\\A");
            String response = inputStreamScanner.next();
            inputStreamScanner.close();
            Log.d("WEB", "Web Service Response ->" + response);

            JSONObject json = new JSONObject(response);

            String observation = json
                    .getJSONObject("weatherObservation")
                    .getString("observation");

            String[] values = observation.split("\\s");

            String slpString = null;
            for(int i=0; i< values.length; i++){
                String value = values[i];
                if(value.startsWith(SLP_STRING.toLowerCase()) || value.startsWith(SLP_STRING.toUpperCase())){
                    slpString = value.substring(SLP_STRING.length());
                    break;
                }
            }

            StringBuffer sb = new StringBuffer(slpString);
            sb.insert(sb.length() -1, ".");
            float val1 = Float.parseFloat("10" + sb);
            float val2 = Float.parseFloat("09" + sb);

            mslp = (Math.abs((1000-val1)) < Math.abs((1000-val2))) ? val1 : val2;
        } catch (Exception e){
            Log.e("EXCEPTION!!", "Could not communcate with web service", e);
        } finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
        }

        return mslp;
    }

    @Override
    protected void onPostExecute(Float result) {
        MainActivity.THIS_MSLP = result;
        MainActivity.WEB_SERVICE_FETCHING = false;
    }
}
