package com.example.stock_watch;

import android.net.Uri;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NameDownloader extends AsyncTask<String, Void, String> {

    private static final String SUPPORTED_STOCK = "https://api.iextrading.com/1.0/ref-data/symbols";
    private static HashMap<String, String> hashMap = new HashMap<>();

    public ArrayList<Stock> match(String s){
        ArrayList<Stock> arrayList = new ArrayList<>();
        for(Map.Entry<String, String > entry : hashMap.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            String symbolCheck = key.toLowerCase();
            String nameCheck = value.toLowerCase();
            String keyWord = s.toLowerCase();
            if(symbolCheck.contains(keyWord) || nameCheck.contains(keyWord)){
                if (!(key.isEmpty() || key == null || value.isEmpty() || value == null )){
                    arrayList.add(new Stock(key, value));
                }
            }
        }
        return arrayList;
    }

    @Override
    protected String doInBackground(String... params) {
        Uri dataUri = Uri.parse(SUPPORTED_STOCK);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
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
        try{
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                String symbol = jsonObject.getString("symbol");
                String name = jsonObject.getString("name");
                hashMap.put(symbol,name);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
