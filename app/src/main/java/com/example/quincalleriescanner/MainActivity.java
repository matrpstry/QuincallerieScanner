package com.example.quincalleriescanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    
    private DecoratedBarcodeView barcodeScanner;
    private EditText manualBarcodeInput;
    private TextView barcodeText;
    private RecyclerView resultsRecyclerView;
    private ResultAdapter resultAdapter;
    private ProgressBar progressBar;
    private Button scanAgainButton;
    private Button searchButton;
    private TextView errorText;
    
    private DatabaseHelper dbHelper;
    private String lastScannedBarcode = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Initialize views
        barcodeScanner = findViewById(R.id.barcodeScanner);
        manualBarcodeInput = findViewById(R.id.manualBarcodeInput);
        barcodeText = findViewById(R.id.barcodeText);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        scanAgainButton = findViewById(R.id.scanAgainButton);
        searchButton = findViewById(R.id.searchButton);
        errorText = findViewById(R.id.errorText);
        
        // Setup RecyclerView
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultAdapter = new ResultAdapter(new ArrayList<>());
        resultsRecyclerView.setAdapter(resultAdapter);
        
        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startBarcodeScanner();
        }
        
        // Manual search button
        searchButton.setOnClickListener(v -> {
            String barcode = manualBarcodeInput.getText().toString().trim();
            if (!barcode.isEmpty()) {
                onBarcodeScanned(barcode);
            } else {
                Toast.makeText(this, "Veuillez entrer un code-barres", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Scan again button click listener
        scanAgainButton.setOnClickListener(v -> {
            resetScanner();
        });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, 
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanner();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show();
                // Continue with manual input only
                manualBarcodeInput.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void startBarcodeScanner() {
        try {
            barcodeScanner.decodeSingle(new BarcodeCallback() {
                @Override
                public void barcodeResult(BarcodeResult result) {
                    if (result.getText() != null && !result.getText().equals(lastScannedBarcode)) {
                        lastScannedBarcode = result.getText();
                        runOnUiThread(() -> onBarcodeScanned(lastScannedBarcode));
                    }
                }
                
                @Override
                public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                    // Not used
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Scanner error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                manualBarcodeInput.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
            });
        }
    }
    
    private void onBarcodeScanned(String barcode) {
        try {
            runOnUiThread(() -> {
                // Show progress and scanned barcode
                progressBar.setVisibility(View.VISIBLE);
                barcodeText.setText("Barcode: " + barcode);
                barcodeText.setVisibility(View.VISIBLE);
                errorText.setVisibility(View.GONE);
                resultsRecyclerView.setVisibility(View.GONE);
                scanAgainButton.setVisibility(View.GONE);
                
                // Search in database
                List<SupplierResult> results = dbHelper.searchByEAN(barcode);
                
                // Update UI with results
                progressBar.setVisibility(View.GONE);
                resultAdapter.updateResults(results);
                resultsRecyclerView.setVisibility(View.VISIBLE);
                scanAgainButton.setVisibility(View.VISIBLE);
                
                // Show message if product not found
                boolean productFound = false;
                for (SupplierResult result : results) {
                    if (result.getSupplierReference() != null && !result.getSupplierReference().equals("Non r\u00e9f\u00e9renc\u00e9")) {
                        productFound = true;
                        break;
                    }
                }
                
                if (!productFound) {
                    errorText.setText("Produit non trouv\u00e9. Essayez: 3000000000001, 3000000000002, 123456789012");
                    errorText.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }
    
    private void resetScanner() {
        lastScannedBarcode = null;
        barcodeText.setText("Barcode: ");
        barcodeText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        resultsRecyclerView.setVisibility(View.GONE);
        scanAgainButton.setVisibility(View.GONE);
        
        // Clear results
        resultAdapter.updateResults(new ArrayList<>());
        
        // Resume scanning
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            try {
                barcodeScanner.resume();
            } catch (Exception e) {
                Toast.makeText(this, "Resume error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            try {
                barcodeScanner.resume();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        try {
            barcodeScanner.pause();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            barcodeScanner.pause();
        } catch (Exception e) {
            // Ignore
        }
    }
}
