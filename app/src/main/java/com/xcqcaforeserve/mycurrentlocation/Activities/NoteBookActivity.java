package com.xcqcaforeserve.mycurrentlocation.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xcqcaforeserve.mycurrentlocation.Adapter.NoteBookAdapter;
import com.xcqcaforeserve.mycurrentlocation.ClickListner.MyDividerItemDecoration;
import com.xcqcaforeserve.mycurrentlocation.ClickListner.RecyclerTouchListener;
import com.xcqcaforeserve.mycurrentlocation.Database.DatabaseHelper;
import com.xcqcaforeserve.mycurrentlocation.Models.NoteBook;
import com.xcqcaforeserve.mycurrentlocation.R;

import java.util.ArrayList;
import java.util.List;

public class NoteBookActivity extends AppCompatActivity {

    private Context mContext;
    private NoteBookAdapter mAdapter;
    private List<NoteBook> notesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noNotesView;
    private FloatingActionButton fab;

    private DatabaseHelper db;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_book);
        mContext = this;

        recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.empty_notes_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        db = new DatabaseHelper(mContext);

        notesList.addAll(db.getAllNotes());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });

        mAdapter = new NoteBookAdapter(mContext, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(mContext, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    private void createNote(String note) {
        // inserting note in db and getting
        long id = db.insertNote(note);  // newly inserted note id
        NoteBook n = db.getNoteBook(id);  // get the newly inserted note from db
        if (n != null) {
            notesList.add(0, n);   // adding new note to array list at 0 position
            mAdapter.notifyDataSetChanged();  // refreshing the list
            toggleEmptyNotes();
        }
    }

    private void updateNote(String note, int position) {
        NoteBook n = notesList.get(position);
        n.setNote(note);  // updating note text
        db.updateNote(n);    // updating note in db
        notesList.set(position, n);   // refreshing the list
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();
    }

    private void deleteNote(int position) {
        db.deleteNote(notesList.get(position));   // deleting the note from db
        notesList.remove(position);    // removing the note from the list
        mAdapter.notifyItemRemoved(position);
        toggleEmptyNotes();
    }

    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }

    private void showNoteDialog(final boolean shouldUpdate, final NoteBook note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mContext);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputNote.getText().toString())) {    // Show toast message when no text is entered
                    Toast.makeText(mContext, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && note != null) {
                    updateNote(inputNote.getText().toString(), position);   // update note by it's id
                } else {
                    createNote(inputNote.getText().toString());   // create new note
                }
            }
        });
    }

    private void toggleEmptyNotes() {
        if (db.getNotesCount() > 0) {      // you can check notesList.size() > 0
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }
}