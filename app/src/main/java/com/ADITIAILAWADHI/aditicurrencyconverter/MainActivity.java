package com.ADITIAILAWADHI.aditicurrencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    String[] shortLanguages;
    String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shortLanguages = getResources().getStringArray(R.array.shortLanguages);
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        currentLanguage = preferences.getString("my_lang", "EN");
        setLocale(currentLanguage);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.app_name);

        displayTime();

        //click button to get results
        Button convertBtn = findViewById(R.id.convert);
        convertBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText currencyAmount = (EditText) findViewById(R.id.amount);
                String amountStr = currencyAmount.getText().toString();

                if(amountStr.length() == 0){
                    Toast.makeText(MainActivity.this, "Please enter amount to be converted", Toast.LENGTH_SHORT).show();

                    // clear results from previously entered amount
                    EditText clearedResult = findViewById(R.id.result);
                    clearedResult.setText("");
                    TextView clearedResult2 = findViewById(R.id.result2);
                    clearedResult2.setText("");
                }

                else
                {
                    currencyConverter();
                }
        }
        });
    }

    private void currencyConverter() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Get amount data from edittext
                EditText user_enteredAmount = (EditText) findViewById(R.id.amount);
                double amount = Double.parseDouble(user_enteredAmount.getText().toString());

                //get currency values from Spinner
                Spinner selected_currencyFrom = findViewById(R.id.currency_from);
                String fromCurrency = selected_currencyFrom.getSelectedItem().toString();

                //get currency values from Spinner
                Spinner selected_currencyTo = findViewById(R.id.currency_to);
                String toCurrency = selected_currencyTo.getSelectedItem().toString();


                final String rateInfoStr = Helper.getInfo(Helper.RATE_API_CORE + fromCurrency);
                //do things on the main UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rateInfoStr != null){
                            Log.i("rateInfoStr",rateInfoStr);
                            try {
                                JSONObject rootObject = new JSONObject(rateInfoStr);
                                JSONObject rateObject = rootObject.getJSONObject("rates");

                                double convertedAmount = amount * rateObject.getDouble(toCurrency);
                                Log.i("convertedAmount",String.format("%.2f",convertedAmount));

                                //Show results
                                EditText currencyConverted = findViewById(R.id.result);
                                currencyConverted.setText(String.format("%.2f",convertedAmount));

                                TextView final_result = findViewById(R.id.result2);
                                final_result.setText(String.format("%.2f %s = %.2f %s",amount, fromCurrency, convertedAmount, toCurrency));

                                Toast.makeText(MainActivity.this, "Currency Converted", Toast.LENGTH_SHORT).show();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        else {
                            Log.i("rateInfoStr", "No data");
                        }
                    }
                });
            }
        }).start();
    }

    public void clearClicked(View view) {
        EditText clearedResult = findViewById(R.id.result);
        clearedResult.setText("");

        EditText clearedAmount = findViewById(R.id.amount);
        clearedAmount.setText("");

        TextView clearedResult2 = findViewById(R.id.result2);
        clearedResult2.setText("");

        Toast.makeText(MainActivity.this, "All Cleared", Toast.LENGTH_SHORT).show();
    }

    private void displayTime() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //get current time
                String currentTime = Helper.getCurrentTime();
                TextView timeView = findViewById(R.id.timeView);
                timeView.setText(currentTime);
                handler.postDelayed(this,1000);
            }
        });
    }

//    Language Code


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.language).setTitle(currentLanguage);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.language) {
            changeLanguage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeLanguage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Language");
        builder.setSingleChoiceItems(shortLanguages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setLocale(shortLanguages[i]);
                recreate();
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        sharedPreferences.edit().putString("my_lang", lang).apply();
    }
}