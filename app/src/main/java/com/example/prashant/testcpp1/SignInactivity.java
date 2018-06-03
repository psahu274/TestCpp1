package com.example.prashant.testcpp1;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FacebookAuthCredential;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class SignInactivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailid;
    private EditText password;
    private Button signin;
    private Button signup;
    private Button verifyemailid;
    private ProgressDialog mProgressDialog;
    private static final String TAG = "EmailPassword";
    private DatabaseReference mDatabase;
    private LoginButton fblogin;
    private CallbackManager callbackManager;
    private static final String TAG1 = "FacebookLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_sign_inactivity);
        callbackManager = CallbackManager.Factory.create();
        emailid = (EditText) findViewById(R.id.emailid);
        password = (EditText) findViewById(R.id.password);
        signin = (Button) findViewById(R.id.signin_button);
        signup = (Button) findViewById(R.id.signup_button);
        verifyemailid = (Button) findViewById(R.id.verifyemailid_button);
        fblogin = (LoginButton) findViewById(R.id.fblogin_button);
        fblogin.setReadPermissions("email","public_profile");
        fblogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG1,"facebook:onSuccess "+loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG1,"facebook:onError",error);
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateForm()){
                    return;
                }else {
                    createAccount(emailid,password);
                }
            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateForm()){
                    return;
                }else {
                    signinuser(emailid,password);
                }
            }
        });
        verifyemailid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyuser();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token){
        Log.d(TAG1,"handleAccessToken:"+token);
        showProgressDialog();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG1, "signInWithCredential:success");
                        startActivity(new Intent(SignInactivity.this, MainActivity.class));
                        writeSettings(user.getUid(), true, 2);
                        fblogin.setVisibility(View.INVISIBLE);
                        finish();
                    }
                }
                else {
                    Toast.makeText(SignInactivity.this,"Authentication failed",Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog();
            }
        });
    }

    private void verifyuser() {
        final FirebaseUser user = mAuth.getCurrentUser();
        showProgressDialog();
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SignInactivity.this,"Verification email sent to:"+user.getEmail(),Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                    verifyemailid.setEnabled(false);
                    findViewById(R.id.layout_forname).setVisibility(View.VISIBLE);
                    findViewById(R.id.returnto_signin).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            findViewById(R.id.emailidlayout).setVisibility(View.VISIBLE);
                            findViewById(R.id.signactivity_layout).setVisibility(View.VISIBLE);
                            findViewById(R.id.verifyemail_layout).setVisibility(View.GONE);
                        }
                    });
                } else{
                    Log.e(TAG,"sendEmailVerification",task.getException());
                    Toast.makeText(SignInactivity.this,"Failed to send verification email",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void signinuser(EditText emailid, EditText password) {
        String email = emailid.getText().toString().trim();
        String pass = password.getText().toString().trim();
        emailid.getText().clear();
        password.getText().clear();
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email,pass).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG,"signInwithEmail:Successful");
                    final FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && !user.isEmailVerified()){
                        Toast.makeText(SignInactivity.this,"This account need to be verified",Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignInactivity.this);
                        builder.setMessage("Press Ok to send Verification mail to your emailaddress").create();
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                user.sendEmailVerification();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }if(user != null && user.isEmailVerified()){
                        /*boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNew){
                            writeSettings(user.getUid(),true,2);
                            Log.d("isNew",mDatabase.toString());
                        }*/
                        startActivity(new Intent(SignInactivity.this, MainActivity.class));
                        finish();
                    }

                }else{
                    Log.w(TAG,"signInwithEmail:failure",task.getException());
                    Toast.makeText(SignInactivity.this,"Authentification failed",Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog();
            }
        });
    }

    private void createAccount(EditText emailid, EditText password) {
        String email = emailid.getText().toString().trim();
        String pass = password.getText().toString().trim();
        emailid.getText().clear();
        password.getText().clear();
        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null){
                                user.sendEmailVerification();
                                writeSettings(user.getUid(),true,2);
                            }
                            Toast.makeText(SignInactivity.this,"Verification email sent to your emailaddress, verify to signIn",Toast.LENGTH_SHORT).show();

                            /*findViewById(R.id.emailidlayout).setVisibility(View.GONE);
                            findViewById(R.id.signactivity_layout).setVisibility(View.GONE);
                            findViewById(R.id.verifyemail_layout).setVisibility(View.VISIBLE);
                            findViewById(R.id.layout_forname).setVisibility(View.GONE);*/
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //Toast.makeText(SignInactivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e){
                                Toast.makeText(SignInactivity.this,"This email address is already in use by another user",Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailid.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailid.setError("Required.");
            valid = false;
        } else {
            emailid.setError(null);
        }

        String passwordn = password.getText().toString();
        if (TextUtils.isEmpty(passwordn)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    private void writeSettings(String uid,boolean mSound,int mAnimOption){
        SettingsValues values = new SettingsValues(mSound,mAnimOption);
        mDatabase.child(uid).child("settings").setValue(values);
    }

    public void showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog(){
        if (mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }
}
