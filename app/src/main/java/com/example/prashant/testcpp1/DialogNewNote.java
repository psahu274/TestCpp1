package com.example.prashant.testcpp1;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Prashant on 16-03-2018.
 */

public class DialogNewNote extends DialogFragment {
    public String stringTitle;
    public String stringDescription;
    public boolean ideaboolean = false;
    public boolean todoboolean = false;
    public boolean importantboolean = false;
    private DatabaseReference mDatabase,mref;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_note,null);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mref = mDatabase.child(mUser.getUid());
        final EditText editTitle = (EditText) dialogView.findViewById(R.id.editTitle);
        final EditText editDescription = (EditText) dialogView.findViewById(R.id.editDescription);
        final CheckBox checkBoxIdea = (CheckBox) dialogView.findViewById(R.id.checkBoxIdea);
        final CheckBox checkBoxTodo = (CheckBox) dialogView.findViewById(R.id.checkBoxToDo);
        final CheckBox checkBoxImp = (CheckBox) dialogView.findViewById(R.id.checkBoxImportant);
        builder.setView(dialogView).setMessage("Add a New Note!").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (editTitle.getText() != null){
                    stringTitle = editTitle.getText().toString();
                }
                if (editDescription.getText() != null){
                    stringDescription = editDescription.getText().toString();
                }
                ideaboolean = checkBoxIdea.isChecked();
                todoboolean = checkBoxTodo.isChecked();
                importantboolean = checkBoxImp.isChecked();
                addNewNote(stringTitle,stringDescription,ideaboolean,todoboolean,importantboolean);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
        return builder.create();
    }

    private void addNewNote(final String stringTitle, String stringDescription, boolean ideaboolean, boolean todoboolean, boolean importantboolean) {
        String key = mref.child("notes").push().getKey();
        Log.d("NewNote","pushed key:"+key+ideaboolean+todoboolean+importantboolean);
        Note note = new Note(stringTitle,stringDescription,ideaboolean,todoboolean,importantboolean);
        Map<String ,Object> notevalues = note.toMap();
        Map<String ,Object> childUpdate = new HashMap<>();
        childUpdate.put("/"+key,notevalues);
        mref.child("notes").updateChildren(childUpdate, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d("Notes added","Successfully:"+":"+stringTitle);
            }
        });
    }
}
