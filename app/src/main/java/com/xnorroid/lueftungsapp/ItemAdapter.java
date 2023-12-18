package com.xnorroid.lueftungsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<String[]> {

    public ItemAdapter(Context context, int resource, List<String[]> allItems) {
        super(context, resource, allItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String[] singleItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_item, parent, false);
        }
        TextView line1 = convertView.findViewById(R.id.textViewLine1);
        TextView line2 = convertView.findViewById(R.id.textViewLine2);
        TextView item_id = convertView.findViewById(R.id.textViewItem_id);

        line1.setText(singleItem[0]);
        line2.setText(singleItem[1]);
        item_id.setText(singleItem[2]);

        return convertView;
    }
}