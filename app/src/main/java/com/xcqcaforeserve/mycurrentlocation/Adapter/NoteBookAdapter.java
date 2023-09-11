package com.xcqcaforeserve.mycurrentlocation.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xcqcaforeserve.mycurrentlocation.Models.NoteBook;
import com.xcqcaforeserve.mycurrentlocation.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NoteBookAdapter extends RecyclerView.Adapter<NoteBookAdapter.NoteBookViewHolder> {

    private Context context;
    private List<NoteBook> notesList;

    public NoteBookAdapter(Context context, List<NoteBook> notesList) {
        this.context = context;
        this.notesList = notesList;
    }

    @NonNull
    @Override
    public NoteBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_row, parent, false);
        return new NoteBookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteBookViewHolder holder, int position) {
        NoteBook note = notesList.get(position);
        holder.note.setText(note.getNote());
        holder.dot.setText(Html.fromHtml("&#8226;")); // Displaying dot from HTML character code
        holder.timestamp.setText(formatDate(note.getTimestamp()));   // Formatting and displaying timestamp

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd MMM");
            return fmtOut.format(date);
        } catch (Exception e) {
            e.getStackTrace();
        }

        return "";
    }

    public class NoteBookViewHolder extends RecyclerView.ViewHolder {

        public TextView note;
        public TextView dot;
        public TextView timestamp;

        public NoteBookViewHolder(@NonNull View itemView) {
            super(itemView);
            note = itemView.findViewById(R.id.note);
            dot = itemView.findViewById(R.id.dot);
            timestamp = itemView.findViewById(R.id.timestamp);

        }
    }
}