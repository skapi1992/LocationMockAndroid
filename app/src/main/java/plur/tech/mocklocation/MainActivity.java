package plur.tech.mocklocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Timer timer;
    private MockLocationProvider mock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mock = new MockLocationProvider(LocationManager.GPS_PROVIDER, this);
        //Set test location
        mock.pushLocation(52.22957, 21.01026);

        LocationManager locMgr = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        LocationListener lis = new LocationListener() {
            public void onLocationChanged(Location location) {
                //You will get the mock location
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
            //...
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, lis);

        timer = new Timer();
        // This timer task will be executed every 1 sec.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //TODO
                String url = "https://location-mock.appspot.com/_ah/api/location/v1/location/get";
                String response = getResponse(url);
                int indexOfLat = response.indexOf("lat");
                int indexOfLng = response.indexOf("long");
                String latStr = response.substring(indexOfLat + 6, indexOfLat + 14);
                String lngStr = response.substring(indexOfLng + 7, indexOfLng + 15);
                Float latitude = Float.parseFloat(latStr);
                Float longitude = Float.parseFloat(lngStr);
                System.out.println(latitude + " " + longitude);
                mock.pushLocation(latitude, longitude);
/*
                Location location = new Location(LocationManager.GPS_PROVIDER);
                Double lat = Double.valueOf(latitude);
                location.setLatitude(lat);
                Double lng = Double.valueOf(longitude);
                location.setLongitude(lng);
*/

            }
        }, 0, 3000);

    }

    @Override
    protected void onDestroy() {
        mock.shutdown();
        super.onDestroy();
    }

    public static String getResponse(String page_address) {
        URL url = null;
        try {
            url = new URL(page_address);
        } catch (MalformedURLException e) {
            //DO NOTHING
        }

        if (url == null)
            return "";

        HttpURLConnection urlConnection = null;
        String text = "";
        try {
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            int data = isw.read();
            while (data != -1) {
                Character current = (char) data;
                data = isw.read();
                text = text.concat(current.toString());
            }
            return text;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "";
    }

}
