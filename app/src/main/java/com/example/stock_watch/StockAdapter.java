package com.example.stock_watch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private ArrayList<Stock> stockArrayList;
    private MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> stockArrayList, MainActivity mainActivity) {
        this.stockArrayList = stockArrayList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row, parent, false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Stock stock = stockArrayList.get(position);

        String icon;
        if (stock.getChangedPrice() > 0) {
            icon = "▲";
            setColor(holder, Color.GREEN);
        } else if (stock.getChangedPrice() == 0){
            icon = "";
            setColor(holder, Color.WHITE);
        } else {
            icon = "▼";
            setColor(holder, Color.RED);
        }

        holder.symbol.setText(stock.getSymbol());
        holder.name.setText(stock.getCompanyName());
        holder.price.setText(String.format("%.2f", stock.getPrice()));
        holder.changeInfo.setText(String.format("%s%.2f(%.2f%%)", icon, stock.getChangedPrice(), stock.getChangedPercentage() * 100));
    }

    @Override
    public int getItemCount() {
        return stockArrayList.size();
    }

    public void setColor(MyViewHolder holder, int color){
        holder.symbol.setTextColor(color);
        holder.name.setTextColor(color);
        holder.price.setTextColor(color);
        holder.changeInfo.setTextColor(color);
    }
}
