package com.example.stock_watch;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView symbol;
    TextView price;
    TextView changeInfo;
    TextView name;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        symbol = itemView.findViewById(R.id.symbolTextView);
        price = itemView.findViewById(R.id.priceTextView);
        changeInfo = itemView.findViewById(R.id.changeTextView);
        name = itemView.findViewById(R.id.nameTextView);
    }
}
