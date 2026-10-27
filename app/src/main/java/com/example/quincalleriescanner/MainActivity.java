package com.example.quincalleriescanner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class MainActivity extends AppCompatActivity implements SupplierScraper.ScrapeListener {
    
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    
    private DecoratedBarcodeView barcodeScanner;
    private TextView barcodeText;
    private RecyclerView resultsRecyclerView;
    private ResultAdapter resultAdapter;
    private ProgressBar progressBar;
    private ProgressBar scrapeProgressBar;
    private Button scanAgainButton;
    private TextView errorText;
    private TextView scrapeStatusText;
    private Button populateDatabaseButton;
    
    private DatabaseHelper dbHelper;
    private SupplierScraper supplierScraper;
    private String lastScannedBarcode = null;
    private int suppliersScraped = 0;
    private int totalProductsFound = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        
        // Initialize views
        barcodeScanner = findViewById(R.id.barcodeScanner);
        barcodeText = findViewById(R.id.barcodeText);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        scanAgainButton = findViewById(R.id.scanAgainButton);
        errorText = findViewById(R.id.errorText);
        scrapeProgressBar = findViewById(R.id.scrapeProgressBar);
        scrapeStatusText = findViewById(R.id.scrapeStatusText);
        populateDatabaseButton = findViewById(R.id.populateDatabaseButton);
        
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
        
        // Initialize supplier scraper
        supplierScraper = new SupplierScraper(this, this);
        
        // Setup populate database button
        populateDatabaseButton.setOnClickListener(v -> {
            showPopulateDatabaseDialog();
        });
        
        // Check if database needs population on first run
        checkDatabasePopulation();
        
        // Scan again button click listener
        scanAgainButton.setOnClickListener(v -> {
            resetScanner();
        });
    }
    
    private void checkDatabasePopulation() {
        // Check if database is empty or has very few products
        int productCount = dbHelper.getProductCount();
        if (productCount <= 2) {
            // Database has only sample data, ask user if they want to populate
            showPopulateDatabaseDialog();
        }
    }
    
    private void showPopulateDatabaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Base de données vide");
        builder.setMessage("La base de données contient seulement des exemples. Voulez-vous scraper les catalogues des fournisseurs pour obtenir tous les produits ? Cela peut prendre plusieurs minutes et nécessite une connexion Internet.");
        builder.setPositiveButton("Oui, scraper maintenant", (dialog, which) -> {
            startDatabasePopulation();
        });
        builder.setNegativeButton("Non, plus tard", (dialog, which) -> {
            // Continue with sample data
            populateDatabaseButton.setVisibility(View.VISIBLE);
        });
        builder.setCancelable(false);
        builder.create().show();
    }
    
    private void startDatabasePopulation() {
        // Show scraping UI
        scrapeProgressBar.setVisibility(View.VISIBLE);
        scrapeStatusText.setVisibility(View.VISIBLE);
        scrapeStatusText.setText("Début du scraping...");
        populateDatabaseButton.setVisibility(View.GONE);
        
        // Reset counters
        suppliersScraped = 0;
        totalProductsFound = 0;
        
        // Clear existing data (except suppliers)
        dbHelper.clearAllProducts();
        
        // Start scraping
        supplierScraper.startScraping();
    }
    
    @Override
    public void onScrapeStarted(String supplier) {
        runOnUiThread(() -> {
            scrapeStatusText.setText("Scraping " + supplier + "...");
        });
    }
    
    @Override
    public void onScrapeProgress(String supplier, int productsFound, int totalPages) {
        runOnUiThread(() -> {
            totalProductsFound += productsFound;
            scrapeStatusText.setText(String.format("Scraping %s: %d produits trouvés", supplier, productsFound));
        });
    }
    
    @Override
    public void onScrapeCompleted(String supplier, int productsAdded) {
        runOnUiThread(() -> {
            suppliersScraped++;
            totalProductsFound += productsAdded;
            scrapeStatusText.setText(String.format("Terminé: %s (%d produits). Total: %d produits, %d fournisseurs traités", 
                    supplier, productsAdded, totalProductsFound, suppliersScraped));
            
            // Check if all suppliers are done
            if (suppliersScraped >= 5) {
                // All done
                scrapeProgressBar.setVisibility(View.GONE);
                scrapeStatusText.setText(String.format("Scraping terminé! %d produits ajoutés", totalProductsFound));
                populateDatabaseButton.setVisibility(View.GONE);
                
                // Show completion message
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    scrapeStatusText.setVisibility(View.GONE);
                }, 5000);
            }
        });
    }
    
    @Override
    public void onScrapeError(String supplier, String error) {
        runOnUiThread(() -> {
            suppliersScraped++;
            scrapeStatusText.setText(String.format("Erreur avec %s: %s. %d fournisseurs traités", 
                    supplier, error, suppliersScraped));
            
            // Check if all suppliers are done
            if (suppliersScraped >= 5) {
                scrapeProgressBar.setVisibility(View.GONE);
                populateDatabaseButton.setVisibility(View.VISIBLE);
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    scrapeStatusText.setVisibility(View.GONE);
                }, 5000);
            }
        });
    }
    
    @Override
    public void onAllScrapingCompleted(int totalProducts) {
        runOnUiThread(() -> {
            scrapeProgressBar.setVisibility(View.GONE);
            scrapeStatusText.setText(String.format("Scraping terminé! %d produits ajoutés", totalProducts));
            populateDatabaseButton.setVisibility(View.GONE);
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                scrapeStatusText.setVisibility(View.GONE);
            }, 5000);
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
                if (result.getSupplierReference() != null && !result.getSupplierReference().equals("Non référencé")) {
                    productFound = true;
                    break;
                }
            }
            
            if (!productFound) {
                errorText.setText("Produit non trouvé. Essayez: 123456789012, 1234567890, 3000000000001, 3000000000002");
                errorText.setVisibility(View.VISIBLE);
            }
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
