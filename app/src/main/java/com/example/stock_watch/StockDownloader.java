package com.example.stock_watch;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader extends AsyncTask<String, Integer, String>{

    private MainActivity mainActivity;

    public StockDownloader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder sb = new StringBuilder();
        String urlToUse = params[0];
        try{
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while((line = reader.readLine()) != null){
                sb.append(line).append('\n');
            }
        } catch (Exception e){
            return null;
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        Stock stock = parseJSON(s);
        if (stock != null) {
            mainActivity.updateData(stock);
        }
    }

    private Stock parseJSON(String s){
        Stock stock;
        try{
            JSONObject jsonObject = new JSONObject((s));

            String symbol = jsonObject.getString("symbol");
            String companyName = jsonObject.getString("companyName");

            String priceString = jsonObject.getString("latestPrice");
            double price = (priceString == "null" ? 0.0: Double.valueOf(priceString));

            String changeString = jsonObject.getString("change");
            double change = (changeString == "null" ? 0.0: Double.valueOf(changeString));

            String changePercentString = jsonObject.getString("changePercent");
            double changePercent = (changePercentString == "null" ? 0.0: Double.valueOf(changePercentString));

            stock = new Stock(symbol,companyName, price, change, changePercent);
            return stock;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
