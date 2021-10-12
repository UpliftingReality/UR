package com.example.gsptracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import android.content.Context;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;
import android.Manifest;
import java.util.ArrayList;
import android.content.Intent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.content.pm.PackageManager;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

//import jdk.internal.org.jline.utils.InputStreamReader;

//import org.json.JSONException;
//import org.json.JSONObject;

import java.io.IOException;
// import java.net.MalformedURLException;
// import java.net.ProtocolException;
import java.util.Scanner;



public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSIONS_FINE_LOCATION = 99;//permission value is a arbitrary value
    private LocationCallback locationCallBack;

    private ArFragment arCam;
    public int count = 0;
    private int clickNo = 0;
    private String messages = "";




    //Location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;

    TextView tv_lat, tv_lon;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        //tv_serverReply = findViewById(R.id.tv_serverReply);

        //set all properties of locationRequest
        locationRequest = new LocationRequest();

        //default location check interval
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        //fast location check interval
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //event triggered whenever update interval is met
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //update UI and server with current coordinates
                update(locationResult.getLastLocation());
            }
        };
        //updateGPS();

        //AR stuff>>>>>>>>>>>
        ArrayList<String> comments = new ArrayList<String>();
        comments.add("have a good day");
        if (messages != "") {
            comments.add(messages);
        }
        System.out.println("ARRAY OF MESSAGES");
        System.out.print(messages);
        if (checkSystemSupport(this)) {
            Button button = (Button) findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText text = (EditText) findViewById(R.id.commentEdit);
                    String post = text.getText().toString();
                }
            });
            arCam = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arCameraArea);
            updateGPS();
            arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                if (this != null) {
                    clickNo++;
                    if (true) {
                        if (messages != "" && !comments.contains(messages)) {
                            comments.add(messages);
                        }
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arCam.getArSceneView().getScene());
                        TransformableNode model = new TransformableNode(arCam.getTransformationSystem());
                        model.setParent(anchorNode);
                        model.select();
                        Node node = new Node();
                        ViewRenderable.builder()
                                .setView(this, R.layout.comment)
                                .build()
                                .thenAccept(renderable -> {
                                    //placeComment(node, renderable, x)
                                    if (count >= comments.size()){
                                        count = 0;
                                    }
                                    String x = comments.get(count);
                                    count += 1;
                                    TextView t = renderable.getView().findViewById(R.id.post);
                                    t.setText(x);
                                    node.setRenderable(renderable);
                                    node.setEnabled(true);
                                    model.addChild(node);
                                });
                    }
                } else {
                    Toast error = Toast.makeText(arCam.getContext(), "Something went wrong", Toast.LENGTH_SHORT);
                    error.show();
                }
            });
        }//END of AR stuff>>>>>>



    }//end onCreate



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "THis app requires GPS permissions", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void updateGPS() {
        //get permissions from the user to track GPS
        //get the current location from the fused client
        //update the ui to display the coordinates

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
//
//            return;
//        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        //check permissions
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user granted permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permission so send the values of location to this function to update ui and send to the server
                    update(location);
                }
            });
        }
        else{
            //user did not grant permission
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }//end of update GPS


    private String dataBaseSendAndRecieve(double lat, double lon) throws IOException, JSONException {
        URL url = new URL("https://byui-upr.herokuapp.com/getMessagesByLocation");
        HttpURLConnection urlConnection = null;
        Gson gson = new Gson();
        String body;
        body = gson.toJson(new Info(lat,lon));

        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Content-length", Integer.toString(body.length()));
        urlConnection.setRequestProperty("charset", "utf-8");
        urlConnection.setRequestProperty("Accept", "*/*");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        //stuff for sending info to the server
        OutputStream out = urlConnection.getOutputStream();
        out.write(body.getBytes("utf-8"));

        //stuff for receiving info from the server
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        String serverReply = (readStream(in));

        //debugging output of server response
        //System.out.println(serverReply);
        //Toast.makeText(this, serverReply, Toast.LENGTH_SHORT).show();


        urlConnection.disconnect();
        return serverReply;

        //wait...your still reading this code?? im sorry


    }//end of start database connection

    //use for GET requests to get response form server after every POST request
    public static String readStream(final InputStream _inputStream) throws JSONException {
        String data = "";

        Scanner scanner = null;
        scanner = new Scanner(_inputStream);

        while (scanner.hasNext()){
            data += (scanner.nextLine());
        }
        scanner.close();

        //convert string into a json object and take out just the part we want before returning
        JSONObject obj = new JSONObject(data);
        JSONArray arr = obj.getJSONArray("messsages");
        data = arr.getJSONObject(0).getString("message");

        return data;
    }


    private void update(Location location) {

        final String[] serverReply = {null};//holds server response
        //use serverReply to update VRcore

        //first round down our location coordinates
        double[] coordinates = roundLocation(location.getLatitude(), location.getLongitude());
        System.out.println(coordinates[0]);
        System.out.println(coordinates[1]);

        Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //send location to server and retrieve messages
                    try {
                        //update a variable with the reply from the server based on the location given
                        serverReply[0] = dataBaseSendAndRecieve(coordinates[0],coordinates[1]);
                        //serverReply[0] = dataBaseSendAndRecieve(location.getLatitude(),location.getLongitude());
                        //serverReply[0] = dataBaseSendAndRecieve(43.8,-111.7);

                        //System.out.print("HERE!!!");
                        //String[] output = getResources().getStringArray(R.array.serverReply)
                        //System.out.println(serverReply[0]);
                        messages = serverReply[0];

                        //dataBaseSendAndRecieve(location.getLatitude(),location.getLongitude());

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        //update all the text view objects with current coordinates and server response
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        //tv_serverReply.setText(serverReply);
        //System.out.println("SERVER REPLY TO FOLLOW>>>>>>>>>>>>>>>");
        //System.out.println(serverReply);


        //System.out.print("SERVER REPLY HERE!!!!!!!!!!!!!!!!");
        //System.out.println(messages);



    }

    private double[] roundLocation(double lat,double lon) {
        //# of digits to take after the decimal

        System.out.println("coordinates before rounding");
        System.out.println(lat);
        System.out.println(lon);

        lat = lat * 10;
        lat = Math.floor(lat);
        lat = lat/Math.pow(10,1);

        lon = lon * 10;
        lon = Math.floor(lon);
        lon = lon/Math.pow(10,1);

        lon = -111.7;//yes... i know...

        double[] array = new double[2];


        array[0] = lat;
        array[1] = lon;



        return array;
    }

    public static boolean checkSystemSupport(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();
            if (Double.parseDouble(openGlVersion) >= 3.0) {
                return true;
            } else {
                Toast.makeText(activity, "App needs opengl version 3.0 or later", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
        } else {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }
}