package com.example.prashant.testcpp1;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseListAdapter;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.Query;

import static android.R.attr.button;
import static android.R.attr.mode;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,mRef;
    private FirebaseUser muser;
    private static final String TAG = "MainActivity";
    private static String uid;
    private ListView notelist;
    private RecyclerView.ItemDecoration itemDecoration;
    private ProgressDialog mProgressDialog;
    private FirebaseListAdapter<Note> listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        DatabaseReference presenceRef = FirebaseDatabase.getInstance().getReference("disconnectmessage");
        presenceRef.onDisconnect().setValue("Went Offline");
        muser = mAuth.getCurrentUser();
        if (muser != null) {
            mRef = mDatabase.child(muser.getUid()).child("notes");
        }
        if (muser != null){
            uid = muser.getUid();
        }
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.addnewnote);
        notelist = (ListView) findViewById(R.id.NotesList);
        itemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }
        };
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogNewNote dialog = new DialogNewNote();
                dialog.show(getFragmentManager(),"123");
            }
        });
        if(muser != null) {
            Log.d("Mainactivity", muser.getUid() + " " + mDatabase.toString());
        }
        if (mRef != null) {
            Log.d("Mainactivity:","inside lisadapter");
            Query query = FirebaseDatabase.getInstance().getReference().child(muser.getUid()).child("notes");
            FirebaseListOptions<Note> options = new FirebaseListOptions.Builder<Note>().setQuery(query,Note.class).setLayout(R.layout.dialog_show_note).build();
            listAdapter = new FirebaseListAdapter<Note>(options) {
                @Override
                protected void populateView(final View v, final Note model, final int position) {
                    String des = model.description;
                    Log.d("listadapter", " " + mRef + des + model.idea + model.important + model.title + model.todo);
                    ((TextView) v.findViewById(R.id.txtDescription)).setTextColor(Color.parseColor("#424242"));
                    ((TextView) v.findViewById(R.id.txtDescription)).setText(model.description);
                    ((TextView) v.findViewById(R.id.txtDescription)).setTextSize(15);
                    if (!model.idea) {
                        ((ImageView) v.findViewById(R.id.imgIdea)).setVisibility(View.GONE);
                    }
                    if (!model.important) {
                        v.findViewById(R.id.imgImp).setVisibility(View.GONE);
                    }
                    ((TextView) v.findViewById(R.id.txtTitle)).setTextColor(Color.parseColor("#000000"));
                    ((TextView) v.findViewById(R.id.txtTitle)).setText(model.title);
                    ((TextView) v.findViewById(R.id.txtTitle)).setTextSize(20);
                    if (!model.todo) {
                        v.findViewById(R.id.imgTodo).setVisibility(View.GONE);
                    }
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String one = String.valueOf(getRef(position));
                            Log.d("Onclick"," "+one);
                        }
                    });
                    v.setLongClickable(true);
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            String longkey = String.valueOf(getRef(position));
                            final String[] key = longkey.split("com/");
                            Log.d("LongClickListener"," "+key[0]+" "+key[1]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Do You want to Delete this Note?");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mDatabase.child(key[1]).setValue(null);
                                    notifyDataSetChanged();
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        }
                    });
                }

                @Override
                public void notifyDataSetChanged() {
                    super.notifyDataSetChanged();
                }
            };
            notelist.setAdapter(listAdapter);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (listAdapter != null) {
            listAdapter.startListening();
        }
        updateUI(currentUser);
        Log.d("In onStart"+currentUser," ");
    }

    private void updateUI(FirebaseUser currentUser) {
        Log.d(TAG,"In updateUI"+currentUser);
        if(currentUser == null){
            startActivity(new Intent(MainActivity.this,SignInactivity.class));
            finish();
        }
        if (currentUser != null && !currentUser.isEmailVerified()){
            Toast.makeText(this,"Your Email ID need to be verified",Toast.LENGTH_SHORT).show();
            currentUser.sendEmailVerification();
            showProgressDialog();
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            hideProgressDialog();
            updateUI(null);
        }
        if (currentUser != null && currentUser.isEmailVerified()){

        }
    }

    private void writeSettings(String uid,Boolean mSound,int mAnimOption){
        SettingsValues values = new SettingsValues(mSound,mAnimOption);
        mDatabase.child(uid).child("settings").setValue(values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivity_menu,menu);
        MenuItem itemsettings = menu.findItem(R.id.settings);
        MenuItem itemsignout = menu.findItem(R.id.signout);
        itemsignout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                FirebaseAuth.getInstance().signOut();
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                updateUI(null);
                return true;
            }
        });
        itemsettings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startActivity(new Intent(MainActivity.this,Settings.class));
                return true;
            }
        });
        return true;
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

    @Override
    protected void onStop() {
        super.onStop();
        if (listAdapter != null) {
            listAdapter.stopListening();
        }
    }
}
