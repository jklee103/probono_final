package com.example.ds.jointest;

import android.*;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ds.jointest.utils.AudioWriterPCM;

import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionException;
import com.naver.speech.clientapi.SpeechRecognitionListener;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.naver.speech.clientapi.SpeechRecognizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

 //   Button btn;



    SearchFragment fragment1;
    UserFragment fragment2;
    //BluetoothFragment fragment3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS},1);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
        //int permissionCheck= ContextCompat.checkSelfPermission(MainActivity.class, new String[]{Manifest.permission.SEND_SMS});
        //fragment생성
        fragment1 = new SearchFragment();
        fragment2 = new UserFragment();
        //fragment3=new BluetoothFragment();

        //fragment연결
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment1).commit();

        //tabLayout
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setIcon(R.drawable.home));
        tabs.addTab(tabs.newTab().setIcon(R.drawable.user));
      //  tabs.addTab(tabs.newTab().setText("bluetooth"));
        tabs.setBackgroundColor(Color.parseColor("#f6f4eb"));
        //    tabs.setBackgroundColor(Color.parseColor("#f6f4eb"));
        //  tabs.setSelectedTabIndicatorColor(Color.parseColor("224, 224, 224"));
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                Fragment selected = null;
                if (position == 0) {
                    selected = fragment1;

                } else if (position == 1) {
                    selected = fragment2;
                }
//                }else if(position==2){
//                    selected = fragment3;
//                }

                //select된 탭으로 보이기
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }




        });

    }

}
