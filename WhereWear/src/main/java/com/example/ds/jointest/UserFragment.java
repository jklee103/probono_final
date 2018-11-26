package com.example.ds.jointest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by DS on 2017-12-18.
 */

public class UserFragment  extends android.support.v4.app.Fragment{
    private FirebaseAuth mAuth;
    Button btnJoin;
    Button btnLogin;
    EditText etid,etpw;
    final static String TAG="userfragment";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //xml과 연결
        final ViewGroup  rootview=(ViewGroup)inflater.inflate(R.layout.user,container,false);
        etid=rootview.findViewById(R.id.etEmail2);
        etpw=rootview.findViewById(R.id.etPW2);
        mAuth=FirebaseAuth.getInstance();
        btnJoin=(Button)rootview.findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(rootview.getContext(),JoinActivity.class);
                startActivity(intent);
            }
        });
        btnLogin=(Button)rootview.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mAuth.signInWithEmailAndPassword(etid.getText().toString(), etpw.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                            //Log.d(TAG, "login suc");
                            Toast.makeText(getContext(),"로그인 완료",Toast.LENGTH_SHORT).show();
                        else
                            Log.d(TAG, "login fail");
                    }
                });
            }
        });

        return rootview;
    }
}
