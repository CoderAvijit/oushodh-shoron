package com.oushodh.shoron.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.oushodh.shoron.R;
import com.oushodh.shoron.data.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.VH> {

    public interface Listener {
        void onToggle(Reminder r, boolean enabled);
        void onEdit(Reminder r);
        void onDelete(Reminder r);
    }

    private final List<Reminder> items = new ArrayList<>();
    private final Listener listener;

    public ReminderAdapter(Listener l) { this.listener = l; }

    public void submit(List<Reminder> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Reminder r = items.get(position);
        h.name.setText(r.getMedicineName());
        h.time.setText(formatBengaliTime(r.getHour(), r.getMinute()));
        h.note.setText(r.getNote() == null || r.getNote().isEmpty()
                ? h.itemView.getContext().getString(R.string.no_note) : r.getNote());
        h.status.setText(r.isTakenToday()
                ? h.itemView.getContext().getString(R.string.status_taken)
                : h.itemView.getContext().getString(R.string.status_pending));

        h.toggle.setOnCheckedChangeListener(null);
        h.toggle.setChecked(r.isEnabled());
        h.toggle.setOnCheckedChangeListener((b, c) -> listener.onToggle(r, c));

        h.btnEdit.setOnClickListener(v -> listener.onEdit(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
        h.itemView.setOnClickListener(v -> listener.onEdit(r));
    }

    @Override
    public int getItemCount() { return items.size(); }

    private static String formatBengaliTime(int hour, int minute) {
        String period = hour < 12
                ? "সকাল" : (hour < 16 ? "দুপুর" : (hour < 19 ? "বিকাল" : "রাত"));
        int h12 = hour % 12;
        if (h12 == 0) h12 = 12;
        String hhmm = String.format(Locale.ENGLISH, "%02d:%02d", h12, minute);
        return toBengaliDigits(hhmm) + " " + period;
    }

    private static String toBengaliDigits(String s) {
        char[] map = {'০','১','২','৩','৪','৫','৬','৭','৮','৯'};
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '9') sb.append(map[c - '0']);
            else sb.append(c);
        }
        return sb.toString();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, time, note, status;
        MaterialSwitch toggle;
        ImageButton btnEdit, btnDelete;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            time = v.findViewById(R.id.tv_time);
            note = v.findViewById(R.id.tv_note);
            status = v.findViewById(R.id.tv_status);
            toggle = v.findViewById(R.id.switch_enabled);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
