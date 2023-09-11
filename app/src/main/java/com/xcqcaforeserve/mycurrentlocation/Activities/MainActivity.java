package com.xcqcaforeserve.mycurrentlocation.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.location.LocationListener;
import com.squareup.picasso.Picasso;
import com.xcqcaforeserve.mycurrentlocation.ApiService.ApiServiceClient;
import com.xcqcaforeserve.mycurrentlocation.ApiService.ApiServiceUsers;
import com.xcqcaforeserve.mycurrentlocation.Utility.LocationFinder;
import com.xcqcaforeserve.mycurrentlocation.Models.ImageResponse;
import com.xcqcaforeserve.mycurrentlocation.R;
import com.xcqcaforeserve.mycurrentlocation.Utility.NetworkStatus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private Context mContext;
    private TextView currentAddress_tv, batteryAvg_tv;
    private AppCompatButton voiceRec_Btn;
    private AppCompatImageView apiImage_img;
    private LocationFinder finder;
    private double latitude = 0.0, longitude = 0.0;
    private String address, city, state, postalCode, currentLocation = "", str_averageCurrentM, str_averageCurrentA;
    private static final int MY_LOCATION_PERMISSION_CODE = 101;
    private Runnable runnable;
    private IntentFilter intentFilterStart;
    private int bpccAbs, bpcaAbs, bpcnAbs, bpcAbs, batteryVol;
    private float fullVoltage, maxVoltage, presentAvgCurrent, presentCurrent, presentVoltage, averageCurrent;
    private List<Float> fullVoltageList = new ArrayList<>();
    private List<Integer> batteryCurrentList = new ArrayList<>();
    private AlertDialog.Builder builder;
    private static int sLastCpuCoreCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        finder = new LocationFinder(mContext);
        builder = new AlertDialog.Builder(mContext);

        currentAddress_tv = (TextView) findViewById(R.id.currentAddress_tv);
        batteryAvg_tv = (TextView) findViewById(R.id.batteryAvg_tv);
        voiceRec_Btn = (AppCompatButton) findViewById(R.id.voiceRec_Btn);
        apiImage_img = (AppCompatImageView) findViewById(R.id.apiImage_img);

        voiceRec_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, VoiceRecognizationActivity.class));
            }
        });

        // Code for location of Current Address   success
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                getCurrentAddressOfUser();
//            }
//        });

//        getBatteryVoltageAndAverage();

        if (NetworkStatus.isNetworkAvailable(mContext)) {
            getRandomImage();
        } else {
            Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getRandomImage() {
        try {
            ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("Loading..");
            progressDialog.setMessage("Please wait. While we are checking image information!");
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiServiceUsers apiServiceUsers = ApiServiceClient.getRetrofit().create(ApiServiceUsers.class);
            apiServiceUsers.getImageResponse().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<ImageResponse>() {
                        @Override
                        public void onSuccess(@NonNull ImageResponse imageResponse) {
                            if (imageResponse.getStatus().equalsIgnoreCase("success")) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Picasso.get().load(imageResponse.getMessage()).placeholder(R.mipmap.ic_launcher)
                                        .error(R.mipmap.ic_launcher).into(apiImage_img);
                                getDownloadImageFromUrl(imageResponse.getMessage());
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "image information not found!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            e.getMessage();
                        }
                    });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getDownloadImageFromUrl(String message) {
        try {
            apiImage_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.setMessage("Do you want to download this image ?").setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new AsyncDownloadFile(mContext).execute(message);
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getBatteryVoltageAndAverage() {
        try {
            presentVoltage = getBatteryVoltageStart();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                double bpcc = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                double bpca = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
                double bpcn = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                double bpc = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

                bpccAbs = (int) Math.abs(bpcc);
                bpcaAbs = (int) Math.abs(bpca);
                bpcnAbs = (int) Math.abs(bpcn);
                bpcAbs = (int) Math.abs(bpc);

                int presentBpccLength = numlength(bpccAbs);
                int presentBpcaLength = numlength(bpcaAbs);
                int presentBpcnLength = numlength(bpcnAbs);
                int presentBpcLength = numlength(bpcAbs);

                if (presentBpcaLength >= 1 && presentBpcaLength <= 4) {
                    presentAvgCurrent = (float) bpcaAbs;
                } else if (presentBpcaLength > 4 && presentBpcaLength <= 7) {
                    presentAvgCurrent = (float) (bpcaAbs / 1000);
                } else if (presentBpcaLength > 7 && presentBpcaLength <= 10) {
                    presentAvgCurrent = (float) (bpcaAbs / 1000000);
                } else {
                    presentAvgCurrent = (float) (00);
                }

                if (presentBpcnLength >= 1 && presentBpcnLength <= 4) {
                    presentCurrent = (float) bpcnAbs;
                } else if (presentBpcnLength > 4 && presentBpcnLength <= 7) {
                    presentCurrent = (float) (bpcnAbs / 1000);
                } else if (presentBpcnLength > 7 && presentBpcnLength <= 10) {
                    presentCurrent = (float) (bpcnAbs / 1000000);
                } else {
                    presentCurrent = (float) 00;
                }

                int presentAvgCurrentLength = numlength((int) presentAvgCurrent);
                if (bpcaAbs != 0 && presentAvgCurrentLength > 2) {
                    str_averageCurrentA = (String.valueOf(String.format(Locale.US, "%.2f", (presentAvgCurrent))) + " mA");
                    batteryAvg_tv.setText(str_averageCurrentA);
                } else if (bpcaAbs != 0 && presentAvgCurrentLength <= 2) {
                    batteryCurrentList.add((int) presentCurrent);
                    averageCurrent = (float) calculateAvarage(batteryCurrentList);
                    str_averageCurrentM = (String.valueOf(String.format(Locale.US, "%.2f", (averageCurrent))) + " mA");
                    batteryAvg_tv.setText(str_averageCurrentM);
                } else if (bpcaAbs == 0 && bpcnAbs != 0) {
                    batteryCurrentList.add((int) presentCurrent);
                    averageCurrent = (float) calculateAvarage(batteryCurrentList);
                    str_averageCurrentM = (String.valueOf(String.format(Locale.US, "%.2f", (averageCurrent))) + " mA");
                    batteryAvg_tv.setText(str_averageCurrentM);
                } else {
                    str_averageCurrentM = "Hardware Not Supported";
                    batteryAvg_tv.setText(str_averageCurrentM);
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private double calculateAvarage(List<Integer> marks) {
        Integer sum = 0;
        if (!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    static int numlength(int n) {
        if (n == 0) return 1;
        int l;
        n = Math.abs(n);
        for (l = 0; n > 0; ++l)
            n /= 10;
        return l;
    }

    // start new code for battery information
    private float getBatteryVoltageStart() {
        intentFilterStart = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, intentFilterStart);

        batteryVol = (int) (batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0));
        fullVoltage = (float) (batteryVol * 0.001);

        fullVoltageList.add(fullVoltage);
        maxVoltage = Collections.max(fullVoltageList);

        return fullVoltage;
    }

    private void getCurrentAddressOfUser() {
        try {
            finder = new LocationFinder(mContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_LOCATION_PERMISSION_CODE);
                } else {    // Check if GPS enabled
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finder.canGetLocation()) {
                                Location location = finder.getLocation();
                                latitude = finder.getLatitude();
                                longitude = finder.getLongitude();
//                                System.out.println("Address by Network or GPS GPSTracker :- " + location);
                                if (location != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getAddress(mContext, latitude, longitude);
                                    }
                                }
                            } else {
                                finder.showSettingsAlert();
                            }
                        }
                    });
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                Log.d("Address list :- ", String.valueOf(addresses.size()));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!address.equalsIgnoreCase("") || !(address == null)) {
                            System.out.println("Address by Network or GPS :- " + address);
                            currentLocation = address;
                            currentAddress_tv.setText(currentLocation);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                currentAddress_tv.setBackground(getDrawable(R.drawable.rounded_corner));
                            }
                        } else {
                            currentAddress_tv.setText("Not found your location!");
                            currentAddress_tv.setBackground(null);
                        }
                        refreshCurrentAddressOfUser(5000);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void refreshCurrentAddressOfUser(int milliseconds) {
        Handler handler = new Handler(mContext.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                getCurrentAddressOfUser();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        double speed = location.getSpeed();
        speed = (speed * 3600) / 1000;
        if (location != null) {
            getAddress(mContext, latitude, longitude);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getCurrentAddressOfUser();
                finder.getLocation();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finder.stopUsingGPS();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finder.stopUsingGPS();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finder.stopUsingGPS();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finder.stopUsingGPS();
    }

    private class AsyncDownloadFile extends AsyncTask<String, String, String> {

        private Context context1;
        private ProgressDialog progressDialog;

        public AsyncDownloadFile(Context context) {
            this.context1 = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context1);
            progressDialog.setMessage("File is downloading. Please wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(true);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            int count;
            try {
                URL url = new URL(strings[0]);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                // show progress bar 0-100%
                int fileLength = urlConnection.getContentLength();
                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
                OutputStream outputStream = new FileOutputStream("/sdcard/downloadedfile.jpg");

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / fileLength));
                    outputStream.write(data, 0, count);
                }

                outputStream.flush();  // flushing output
                outputStream.close();   // closing streams
                inputStream.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String imagePath = Environment.getExternalStorageDirectory().toString() + "/downloadedfile.jpg";
            System.out.println("Image Path :- " + imagePath);

        }
    }
}