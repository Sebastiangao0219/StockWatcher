package com.example.stock_watch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private ArrayList<Stock> stockList = new ArrayList<>();
    private ArrayList<Stock> fileStockList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private SwipeRefreshLayout swiper;
    private ConnectivityManager connectivityManager;
    private boolean loadFromFile = true;
    private String noNetMessageForUpdate ="Stocks Cannot Be Updated Without A Network Connection";
    private String noNetMessageForLoad = "Stocks Cannot Be Loaded Without A Network Connection";
    private String noNetMessageForAdd = "Stocks Cannot Be Added Without A Network Connection";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Stock Watch");
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getUserStock(noNetMessageForLoad);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addMenu){
            if(isNetWorkConnected() == true){
                addStockDialog();
                return true;
            } else{
                noNetWorkDialog(noNetMessageForAdd);
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        final Stock stock = stockList.get(pos);
        String url = String.format("http://www.marketwatch.com/investing/stock/%s", stock.getSymbol());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        final Stock deleteStock = stockList.get(pos);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon_delete);
        builder.setTitle("Delete Stock");
        builder.setMessage(String.format("Delete Stock Symbol %s?", deleteStock.getSymbol()));
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stockList.remove(pos);
                saveToFile();
                stockAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

    public void getUserStock(String dialogMessage){
        loadFile();
        if(isNetWorkConnected() == true){

            if (dialogMessage == noNetMessageForLoad) {
                new NameDownloader().execute();
            }
            if (fileStockList != null) {
                for(Stock stock : fileStockList){
                    doStockDownload(stock);
                }
            }
        } else{
            noNetWorkDialog(dialogMessage);
            if (fileStockList != null) {
                for (Stock stock : fileStockList) {
                    stockList.add(new Stock(stock.getSymbol(), stock.getCompanyName()));
                }
                sortStock(fileStockList);
                stockAdapter.notifyDataSetChanged();
            }
        }
    }

    public void doRefresh() {
        stockList.clear();
        getUserStock(noNetMessageForUpdate);
        swiper.setRefreshing(false);
    }


    public boolean isNetWorkConnected(){
        if(connectivityManager == null) {
            connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }


    private void doStockDownload(Stock stock) {
        new StockDownloader(this).execute(String.format
                ("https://cloud.iexapis.com/stable/stock/%s/quote?token=sk_1187990500e4429ca36f55bd04aae469",stock.getSymbol()));
    }

    public void updateData(Stock stock){
        if (stock == null) {
            return;
        }
        if (isContainStock(stock)){
            if(loadFromFile != true) {
                duplicateStockInfoDialog(stock.getSymbol());
                loadFromFile = true;
            }
            return;
        } else {
            stockList.add(stock);
            sortStock(stockList);
            saveToFile();
            stockAdapter.notifyDataSetChanged();
        }
    }

    private boolean isContainStock(Stock stock) {
        for(Stock stockInList: stockList){
            if (stockInList.getSymbol().equals(stock.getSymbol()))
                return true;
        }
        return false;
    }

    public void sortStock(ArrayList<Stock> list){
        if(list != null){
            Collections.sort(list, new Comparator<Stock>(){
                public int compare(Stock s1, Stock s2) {
                    return s1.getSymbol().compareTo(s2.getSymbol());
                }
            });
        }
    }

    private void addStockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(input);

        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol:");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String symbolString = input.getText().toString();
                final ArrayList<Stock> matchedStockList = new NameDownloader().match(symbolString);
                sortStock(matchedStockList);
                doOperation(symbolString, matchedStockList);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doOperation(String symbolString, ArrayList<Stock> matchedStockList) {
        if (matchedStockList.size() > 1){
            makeSelectionDialog(matchedStockList);
        } else if (matchedStockList.size() == 1){
            doStockDownload(matchedStockList.get(0));
        } else{
            symbolNotFoundDialog(symbolString);
        }
        loadFromFile = false;
    }

    public void makeSelectionDialog(final ArrayList<Stock> arrayList){

        final String[] items = new String[arrayList.size()];
        for(int i = 0; i< arrayList.size(); i++){
            items[i] = arrayList.get(i).getSymbol() + " - " + arrayList.get(i).getCompanyName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Stock stock = arrayList.get(which);
                doStockDownload(stock);
            }
        });

        builder.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog selectionDialog = builder.create();
        selectionDialog.show();
    }

    private void symbolNotFoundDialog(String symbolString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found: " + symbolString);
        builder.setMessage("Data for stock symbol");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void duplicateStockInfoDialog(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.icon1);
        builder.setTitle("Duplicate Stock");
        builder.setMessage(String.format("Stock Symbol %s is already displayed",s ));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void noNetWorkDialog(String string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage(string);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadFile() {
        Stock stock;
        ArrayList<Stock> list = new ArrayList<>();
        try {
            InputStream inputStream = openFileInput(getString(R.string.file_name));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(sb.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String symbol = jsonObject.getString("symbol");
                String companyName = jsonObject.getString("companyName");

                String priceString = jsonObject.getString("latestPrice");
                double price = (priceString == "null" ? 0.0: Double.valueOf(priceString));

                String changeString = jsonObject.getString("change");
                double change = (changeString == "null" ? 0.0: Double.valueOf(changeString));

                String changePercentString = jsonObject.getString("changePercent");
                double changePercent = (changePercentString == "null" ? 0.0: Double.valueOf(changePercentString));

                stock = new Stock(symbol, companyName, price, change, changePercent);
                if (stock != null) {
                    list.add(stock);
                }
            }

            fileStockList = list;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFiles() throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Stock stock : stockList) {
            JSONObject stockJSON = new JSONObject();
            stockJSON.put("symbol", stock.getSymbol());
            stockJSON.put("companyName", stock.getCompanyName());
            stockJSON.put("latestPrice", stock.getPrice());
            stockJSON.put("change",stock.getChangedPrice());
            stockJSON.put("changePercent",stock.getChangedPercentage());

            jsonArray.put(stockJSON);
        }

        String jsonText = jsonArray.toString();
        FileOutputStream fos = openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

        fos.write(jsonText.getBytes());
        fos.close();
    }

    private void saveToFile(){
        try {
            saveFiles();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
