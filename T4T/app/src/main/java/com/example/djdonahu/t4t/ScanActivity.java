package com.example.djdonahu.t4t;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import com.google.zxing.integration.android.*;
import android.widget.Button;
import android.widget.Toast;


public class ScanActivity extends ActionBarActivity {

    private static String SCAN_TAG = "T4T_SCAN";

    private TextView scanResults;
    // initialize View variables (TextViews, ListViews, Buttons, etc)
    private Button purchasedButton;
    private Button nopeButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Intent intent = getIntent();
        boolean shouldScan = intent.getBooleanExtra("startScanning", false);
        intent.putExtra("startScanning", false);

        // Don't accidentally get stuck in a scanning loop, only scan on first creation
        if (shouldScan) {
            Log.d("T4T", "Launching scan in create");
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //point our View variables to the id that they correspond to
        purchasedButton = (Button) findViewById(R.id.purchased_button);
        nopeButton = (Button) findViewById(R.id.nope_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);


        //define the behaviors for when each button is clicked.
        purchasedButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                Toast toast = Toast.makeText(getApplicationContext(), "Item stored, returning to scanner", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        nopeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                Toast toast = Toast.makeText(getApplicationContext(), "Item discarded, returning to scanner", Toast.LENGTH_SHORT);
                toast.show();
                scanAgain(v);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ScanActivity.this, StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    // Returning from scan app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("T4T", "Scan activity result");
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            // handle scan result
            String format = scanResult.getFormatName();
            if (!format.equals("UPC_A")) {
                Log.w(SCAN_TAG, "Possibly unsupported UPC type " + format);
            }
            String upc = scanResult.getContents();
            Log.d(SCAN_TAG, "UPC: " + upc);
            // To the internet!
            OutpanRequest.getProduct(
                    upc,
                    new FetchUrlCallback() {

                        @Override
                        public void execute(Object result) {
                            Product p = getProduct(result);
                            if (p != null) {
                                Log.d(SCAN_TAG, p.toString());
                            }
                        }
                    }
            );
        }
    }

    // Cast the product
    public Product getProduct(Object result) {
        Product product = null;
        try {
            product = (Product) result;
            // now do something with product result.
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return product;
    }

    //
    // Buttons
    //

    // Scan again
    public void scanAgain (View v) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_scan, container, false);
            return rootView;
        }
    }
}
