package ch.teko.httpexample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = "MainActivity";
    private final String api_url = "http://172.20.10.7:8080/api";
    private String shoppingListJSON = "";
    private EditText editText_new_item;
    private String newItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate() called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "onCreate() ended");
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart() called");
        super.onStart();
        Log.d(LOG_TAG, "onStart() ended");
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume() called");
        super.onResume();

        loadTable();

        // hide keyboard when not focused on editText_new_item
        editText_new_item = findViewById(R.id.editText_new_item);
        editText_new_item.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        Log.d(LOG_TAG, "onResume() ended");
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause() called");
        super.onPause();
        Log.d(LOG_TAG, "onPause() called");
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop() called");
        super.onStop();
        Log.d(LOG_TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy() called");
    }

    private void loadTable() {
        getShoppingListJSON();
        Log.d(LOG_TAG, shoppingListJSON);

        TableLayout tableLayout = findViewById(R.id.tableLayoutContents);

        // clear table when reloading
        if (tableLayout.getChildCount() != 0) {
            tableLayout.removeAllViews();
            Log.d(LOG_TAG, "tableLayout all children removed");
        }

        try {
            JSONArray jsonArray = new JSONArray(shoppingListJSON);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObject = jsonArray.getJSONObject(i);
                String id = itemObject.getString("id");
                String name = itemObject.getString("name");
                String timeInMillis = itemObject.getString("time");
                String marked = itemObject.getString("marked");

                Date date = new Date(Long.parseLong(timeInMillis));
                String date_str = DateFormat.format("MM/dd/yyyy", date).toString();

                Boolean marked_boolean = false;
                if (marked.equals("0")) {
                    marked_boolean = false;
                } else {
                    marked_boolean = true;
                }

                TableRow tableRow = new TableRow(this);

                TextView tv_item = new TextView(this);
                tv_item.setText(name);
                tv_item.setTextColor(Color.BLUE);
                tv_item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tv_item.setGravity(Gravity.CENTER);
                tableRow.addView(tv_item);

                TextView tv_date = new TextView(this);
                tv_date.setText(date_str);
                tv_date.setTextColor(Color.BLUE);
                tv_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                tv_date.setGravity(Gravity.CENTER);
                tableRow.addView(tv_date);

                CheckBox checkBox = new CheckBox(this);
                checkBox.setId(Integer.parseInt(id)); // set checkBox id to item id from Database
                checkBox.setChecked(marked_boolean);
                checkBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); // checkbox align right
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        final int itemId = buttonView.getId();
                        Thread communicationThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new URL(api_url + "/?command=mark&id=" + itemId).openStream();

                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        communicationThread.start();
                    }
                });

                tableRow.addView(checkBox);
                tableLayout.addView(tableRow);
                Log.d(LOG_TAG, "tableLayout one TableRow added");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // called inside loadTable()
    private void getShoppingListJSON() {

        Thread communicationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = new URL(api_url).openStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    StringBuffer buffer = new StringBuffer();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        buffer.append(line);
                    }
                    shoppingListJSON = buffer.toString();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        communicationThread.start();

        // *** wait until the communicationThread to get transportsJsonStr from url
        try {
            communicationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void onButtonAddClicked(View v) {
        editText_new_item.clearFocus();
        newItem = editText_new_item.getText().toString();
        editText_new_item.setText("");

        Thread communicationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new URL(api_url + "/?command=insert&name=" + newItem).openStream();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        communicationThread.start();

        // *** wait until the communicationThread to get transportsJsonStr from url
        try {
            communicationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loadTable();
    }

    public void onButtonDeleteClicked(View v) {
        Thread communicationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new URL(api_url + "/?command=delete").openStream();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        communicationThread.start();

        // *** wait until the communicationThread to get transportsJsonStr from url
        try {
            communicationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loadTable();
    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
