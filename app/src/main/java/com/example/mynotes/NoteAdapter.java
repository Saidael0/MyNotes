package com.example.mynotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> noteList;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NoteAdapter(Context context, List<Note> noteList, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.textNom.setText(note.getNom());
        holder.textDescription.setText(note.getDescription());
        holder.textDate.setText(note.getDate());
        holder.textPriorite.setText(note.getPriorite());

        // Afficher/cacher l'icône photo
        if (note.getPhotoPath() != null && !note.getPhotoPath().isEmpty()) {
            holder.iconPhoto.setVisibility(View.VISIBLE);
        } else {
            holder.iconPhoto.setVisibility(View.GONE);
        }

        // Définir les couleurs selon la priorité
        int backgroundColor, borderColor, textColor;
        switch (note.getPriorite()) {
            case "Haute":
                backgroundColor = R.color.priority_high_bg;
                borderColor = R.color.priority_high;
                textColor = R.color.priority_high;
                break;
            case "Moyenne":
                backgroundColor = R.color.priority_medium_bg;
                borderColor = R.color.priority_medium;
                textColor = R.color.priority_medium;
                break;
            default: // Basse
                backgroundColor = R.color.priority_low_bg;
                borderColor = R.color.priority_low;
                textColor = R.color.priority_low;
                break;
        }

        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColor));
        holder.priorityIndicator.setBackgroundColor(ContextCompat.getColor(context, borderColor));
        holder.textPriorite.setTextColor(ContextCompat.getColor(context, textColor));

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void updateNotes(List<Note> newNotes) {
        this.noteList = newNotes;
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View priorityIndicator;
        TextView textNom, textDescription, textDate, textPriorite;
        ImageView iconPhoto;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            textNom = itemView.findViewById(R.id.textNom);
            textDescription = itemView.findViewById(R.id.textDescription);
            textDate = itemView.findViewById(R.id.textDate);
            textPriorite = itemView.findViewById(R.id.textPriorite);
            iconPhoto = itemView.findViewById(R.id.iconPhoto);
        }
    }
}