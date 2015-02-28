package com.philliplemons.homework2;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class MyActivity extends FragmentActivity
    implements OnMapReadyCallback {

    private GoogleMap myMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        myMap = map;
    }

    public void goToLoc(View view) {
        EditText editText = (EditText) findViewById(R.id.edit_location);
        String loc = editText.getText().toString();
        System.out.println("Location: " + loc);
        loc = loc.replace(" ", "%20");
        new getLatLngTask().execute(loc);
        //myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.0, -97.89), 5));
        //System.out.println("Lat, Lng: " + getLatLongFromAddress(loc));
    }

    private class getLatLngTask extends AsyncTask<String, Void, LatLng> {

        @Override
        protected LatLng doInBackground(String... params) {
            String address = params[0];
            return getLatLongFromAddress(address);
        }

        @Override
        protected void onPostExecute(LatLng result) {
            if(result != null) {
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(result, 10));
                Marker loc = myMap.addMarker(new MarkerOptions()
                    .position(result)
                    .title("Cool")
                    .snippet("Latitude: " + result.latitude + "\n"
                            +"Longitude " + result.longitude));
                System.out.println("Info window showing");
                loc.showInfoWindow();
            } else {
                System.out.println("ERROR: Bad Lat Lng");
            }
        }

        private LatLng getLatLongFromAddress(String address) {
            // Gotten from http://stackoverflow.com/questions/15711499/get-latitude-and-longitude-with-geocoder-and-android-google-maps-api-v2
            String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                    address + "&sensor=false";
            HttpGet httpGet = new HttpGet(uri);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();
            System.out.println("URI: " + uri);

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
                System.out.println(stringBuilder);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("About to get JSON Object");

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());

                double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                System.out.println("Lat: " + lat + " Lon: " + lng);

                return new LatLng(lat, lng);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
