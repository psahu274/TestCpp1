package com.example.prashant.testcpp1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {
    private DatabaseReference mDatabase,mref;
    private FirebaseAuth mAuth;
    private FirebaseUser muser;
    private CheckBox checkBoxSound;
    private RadioButton animfast;
    private RadioButton animslow;
    private RadioButton animmedium;
    //private int mAnimOption;
    public static final int FAST=0;
    public static final int SLOW=1;
    public static final int NONE=2;
    public RadioGroup animradioGroup;
    public int mAnimOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        muser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d("Settings",muser.getUid());
        mref = mDatabase.child(muser.getUid()).child("settings");
        animradioGroup = (RadioGroup) findViewById(R.id.anim_radiogroup);
        animradioGroup.clearCheck();
        checkBoxSound = (CheckBox) findViewById(R.id.check_soundfx);
        animfast = (RadioButton) findViewById(R.id.anim_fast);
        animmedium = (RadioButton) findViewById(R.id.anim_medium);
        animslow = (RadioButton) findViewById(R.id.anim_slow);
        checkBoxSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                writeSettingsSound(muser.getUid(),b);
            }
        });
        animradioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = (RadioButton) radioGroup.findViewById(i);
                if (rb != null && i>-1){
                    switch (rb.getId()){
                        case R.id.anim_fast:
                            mAnimOption = FAST;
                            break;
                        case R.id.anim_slow:
                            mAnimOption = SLOW;
                            break;
                        case R.id.anim_medium:
                            mAnimOption = NONE;
                            break;
                    }
                    writeSettingsAnim(muser.getUid(),mAnimOption);
                }
            }
        });
    }

    private void writeSettingsAnim(String uid, int mAnimOption) {
        mDatabase.child(uid).child("settings").child("mAnimOption").setValue(mAnimOption);
    }

    @Override
    public void onStart(){
        super.onStart();
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SettingsValues values = dataSnapshot.getValue(SettingsValues.class);
                    if (values.mSound){
                    checkBoxSound.setChecked(true);
                }else{
                    checkBoxSound.setChecked(false);
                }
                switch (values.mAnimOption){
                    case FAST:
                        animradioGroup.check(R.id.anim_fast);
                        break;
                    case SLOW:
                        animradioGroup.check(R.id.anim_slow);
                        break;
                    case NONE:
                        animradioGroup.check(R.id.anim_medium);
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("onCancelled",databaseError.toException());
            }
        });
    }

    private void writeSettingsSound(String uid,boolean mSound){
        mDatabase.child(uid).child("settings").child("mSound").setValue(mSound);
    }
}
