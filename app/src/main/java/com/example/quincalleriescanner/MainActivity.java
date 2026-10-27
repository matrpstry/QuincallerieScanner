package com.example.quincalleriescanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private TextView barcodeText;
    private RecyclerView resultsRecyclerView;
    private ResultAdapter resultAdapter;
    private ProgressBar progressBar;
    private Button scanAgainButton;
    private TextView errorText;
    
    private String lastScannedBarcode = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        barcodeScanner = findViewById(R.id.barcodeScanner);
        barcodeText = findViewById(R.id.barcodeText);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        scanAgainButton = findViewById(R.id.scanAgainButton);
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
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void startBarcodeScanner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null && !result.getText().equals(lastScannedBarcode)) {
                    lastScannedBarcode = result.getText();
                    onBarcodeScanned(lastScannedBarcode);
                }
            }
            
            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                // Not used
            }
        });
    }
    
    private void onBarcodeScanned(String barcode) {
        runOnUiThread(() -> {
            // Show progress and scanned barcode
            progressBar.setVisibility(View.VISIBLE);
            barcodeText.setText("Barcode: " + barcode);
            barcodeText.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
            resultsRecyclerView.setVisibility(View.GONE);
            scanAgainButton.setVisibility(View.GONE);
            
            // Search all suppliers
            new Thread(() -> {
                try {
                    List<SupplierResult> results = SupplierAPI.searchAllSuppliers(barcode);
                    
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        
                        if (results != null && !results.isEmpty()) {
                            // Show results container
                            resultsRecyclerView.setVisibility(View.VISIBLE);
                            scanAgainButton.setVisibility(View.VISIBLE);
                            resultAdapter.updateResults(results);
                        } else {
                            // Show error
                            errorText.setText(R.string.no_results);
                            errorText.setVisibility(View.VISIBLE);
                            scanAgainButton.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        errorText.setText(R.string.error_message + "\n" + e.getMessage());
                        errorText.setVisibility(View.VISIBLE);
                        scanAgainButton.setVisibility(View.VISIBLE);
                    });
                }
            }).start();
        });
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
            barcodeScanner.resume();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            barcodeScanner.resume();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeScanner.pause();
    }
}
