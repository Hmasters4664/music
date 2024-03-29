package com.example.hassani.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.hassani.music.adapters.MusicAdapter;
import com.example.hassani.music.audioclass.audio;
import com.example.hassani.music.datastorage.StorageUtil;
import com.example.hassani.music.listeners.onItemClickListener;
import com.example.hassani.music.service.MediaPlayerService;
import com.example.hassani.music.touch.CTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {

    private MediaPlayerService player;
    boolean serviceBound = false;
    private ArrayList<audio> audioList;
    private ImageButton  playpause;
    private ImageButton fastdorward;
    private SeekBar mSeekBar;
    private ImageButton rewind;
    private MusicAdapter music;
    private RecyclerView mRecyclerview;
    private RecyclerView.LayoutManager mLayoutManager;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final int REQUEST_ID2= 2;
    private com.example.hassani.music.customfonts.MyTextView_Roboto_Regular mDuration;
    private com.example.hassani.music.customfonts.MyTextView_Roboto_Regular mPassed;
    boolean playing=false;
    boolean firststart=false;
    private Handler mSeekbarUpdateHandler = new Handler();
   private int AudIn=0;
    private Runnable mUpdateSeekbar;
    private static final String[] PERM= new String[] {"Manifest.permission.READ_EXTERNAL_STORAGE","Manifest.permission.READ_PHONE_STATE"};
    private static final String[] PERMISSION = new String[] {"Manifest.permission.READ_PHONE_STATE"};


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.hassani.music.STORAGE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerview=(RecyclerView)findViewById(R.id.recycler);
        playpause=findViewById(R.id.btnPlay);
        fastdorward=findViewById(R.id.btnForward);
        rewind = findViewById(R.id.btnBackward);
        mSeekBar = findViewById(R.id.sBar);
        mLayoutManager = new LinearLayoutManager(getBaseContext());
        mDuration =findViewById(R.id.duration);
        mPassed = findViewById(R.id.passedd);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M ){
            checkAndRequestPermissions();
        }

        if (checkAndRequestPermissions()) {
            loadAudioList();
        }

        playpause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if((audioList != null && audioList.size() > 0)) {

                    if (!playing) {

                        if(!firststart)
                        {
                            playAudio(AudIn);
                           // setDuration();
                            mUpdateSeekbar.run();
                            ImageButton b = (ImageButton) v;
                            playing = true;
                            firststart=true;
                            //b.setImageResource(R.drawable.ic_pause_circle_outline);
                        } else {
                            player.resumeMedia();
                            ImageButton b = (ImageButton) v;
                            playing = true;
                            b.setImageResource(R.drawable.ic_pause);
                        }
                    }
                    else{
                        ImageButton b = (ImageButton) v;
                        player.pauseMedia();
                        playing = false;
                        b.setImageResource(R.drawable.ic_play);
                    }
                }

            }
        });

        fastdorward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if((audioList != null && audioList.size() > 0 && serviceBound)) {
                    player.skipToNext();

                }

            }
        });

        rewind.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if((audioList != null && audioList.size() > 0 && serviceBound)) {
                    player.skipToPrevious();

                }

            }
        });



        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mUpdateSeekbar = new Runnable() {
            @Override
            public void run() {

                if(serviceBound) {
                    mSeekBar.setProgress(player.getCurrentPosition());
                    updateDuration(player.getCurrentPosition());

                }
                mSeekbarUpdateHandler.postDelayed(this, 50);
            }
        };





    }


    private void playAudio(String media) {
        //Check is service is active
        playing = true;
        playpause.setImageResource(R.drawable.ic_pause_circle_outline);
        firststart=true;
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            playing = false;
            player.stopSelf();
        }
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                // Save to audioList
                audioList.add(new audio(data, title, album, artist,duration));
            }
            //audioList = new ArrayList<>();



        }   cursor.close();
    }
    private void initRecyclerView() {
        if (audioList != null && audioList.size() > 0) {

            music = new MusicAdapter(audioList,getApplication());
            mRecyclerview.setLayoutManager(mLayoutManager);
            mRecyclerview.setAdapter(music);
            mRecyclerview.addOnItemTouchListener(new CTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    playAudio(index);
                    playing = true;
                    mUpdateSeekbar.run();
                    firststart=true;
                    AudIn=index;

                }
            }));
        }
    }

    private void playAudio(int audioIndex) {
        //Check is service is active
        playing = true;
        setDuration(audioIndex);
        mUpdateSeekbar.run();
        //
        playpause.setImageResource(R.drawable.ic_pause);
        firststart=true;
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }


    }



    private void loadAudioList() {
        loadAudio();
        initRecyclerView();


    }



    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkAndRequestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            int permissionReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            List<String> listPermissionsNeeded = new ArrayList<>();

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        String TAG = "LOG_PERMISSION";
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions

                    if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            ) {
                        Log.d(TAG, "Phone state and storage permissions granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        loadAudioList();
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                      //shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                            showDialogOK("Phone state and storage permissions required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    private void setDuration(int index)
    {
        long miliseconds = Long.parseLong(audioList.get(index).getDuration());

        int minutes = (int) (miliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((miliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        String secondsString;
        String finalTimerString;

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = minutes + ":" + secondsString;

        mDuration.setText(finalTimerString);
        mSeekBar.setMax((int) miliseconds);


    }

    private void updateDuration(int miliseconds)
    {


        int minutes = (int) (miliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((miliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        String secondsString;
        String finalTimerString;

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = minutes + ":" + secondsString;

        mPassed.setText(finalTimerString);


    }






}
