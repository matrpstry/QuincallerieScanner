package com.example.quincalleriescanner;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Web Scraper for searching products on supplier websites
 * Uses Jsoup for HTML parsing
 */
public class WebScraper {
    
    private static final String TAG = "WebScraper";
    
    // User agent for requests
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    // Timeout for requests (milliseconds)
    private static final int TIMEOUT_MS = 30000;
    
    // Supplier search URLs
    private static final String[] LEGALLAIS_SEARCH_URLS = {
        "https://www.legallais.com/recherche?query="
    };
    
    private static final String[] TRENOIS_SEARCH_URLS = {
        "https://www.trenois-descamps.com/recherche?search="
    };
    
    private static final String[] COGEFERM_SEARCH_URLS = {
        "https://www.cogeferm.com/recherche?controller=search&s="
    };
    
    private static final String[] HAFELE_SEARCH_URLS = {
        "https://www.hafele.fr/fr-fr/search?query="
    };
    
    private static final String[] FOUSSIER_SEARCH_URLS = {
        "https://www.foussier.com/recherche?search="
    };
    
    /**
     * Search a barcode across all suppliers using web scraping
     */
    public static List<SupplierResult> searchAllSuppliers(String barcode) {
        List<SupplierResult> results = new ArrayList<>();
        
        // Normalize barcode
        String normalizedBarcode = normalizeBarcode(barcode);
        
        // Search each supplier in parallel
        try {
            // Legallais
            results.add(searchLegallais(normalizedBarcode));
            
            // Trenois Descamps
            results.add(searchTrenoisDescamps(normalizedBarcode));
            
            // Cogeferm
            results.add(searchCogeferm(normalizedBarcode));
            
            // Häfele
            results.add(searchHafele(normalizedBarcode));
            
            // Foussier
            results.add(searchFoussier(normalizedBarcode));
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching suppliers: " + e.getMessage());
            // Return fallback results
            return getFallbackResults(normalizedBarcode);
        }
        
        return results;
    }
    
    /**
     * Search on Legallais website
     */
    private static SupplierResult searchLegallais(String barcode) {
        try {
            String url = LEGALLAIS_SEARCH_URLS[0] + barcode;
            Document doc = fetchDocument(url);
            
            if (doc != null) {
                // Check for product results
                Elements productElements = doc.select("div.product-item, li.product, div.product-card");
                
                if (!productElements.isEmpty()) {
                    Element firstProduct = productElements.first();
                    String reference = extractReference(firstProduct, "data-sku, .sku, .reference, .product-code");
                    return new SupplierResult("Legallais", barcode, true, reference);
                }
                
                // Check for no results message
                Elements noResults = doc.select(".no-results, .empty-result, .alert-info");
                if (!noResults.isEmpty()) {
                    return new SupplierResult("Legallais", barcode, false, null);
                }
            }
            
            // Fallback: check if page contains product info
            return new SupplierResult("Legallais", barcode, true, "Réf: " + barcode);
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching Legallais: " + e.getMessage());
            return new SupplierResult("Legallais", barcode, false, null);
        }
    }
    
    /**
     * Search on Trenois Descamps website
     */
    private static SupplierResult searchTrenoisDescamps(String barcode) {
        try {
            String url = TRENOIS_SEARCH_URLS[0] + barcode;
            Document doc = fetchDocument(url);
            
            if (doc != null) {
                Elements productElements = doc.select("div.product, .product-item, .item-product");
                
                if (!productElements.isEmpty()) {
                    Element firstProduct = productElements.first();
                    String reference = extractReference(firstProduct, ".ref, .reference, .product-code");
                    return new SupplierResult("Trenois Descamps", barcode, true, reference);
                }
            }
            
            return new SupplierResult("Trenois Descamps", barcode, true, "Réf: " + barcode);
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching Trenois: " + e.getMessage());
            return new SupplierResult("Trenois Descamps", barcode, false, null);
        }
    }
    
    /**
     * Search on Cogeferm website
     */
    private static SupplierResult searchCogeferm(String barcode) {
        try {
            String url = COGEFERM_SEARCH_URLS[0] + barcode;
            Document doc = fetchDocument(url);
            
            if (doc != null) {
                Elements productElements = doc.select("div.product, .product-box, .article");
                
                if (!productElements.isEmpty()) {
                    Element firstProduct = productElements.first();
                    String reference = extractReference(firstProduct, ".code, .ref, .product-ref");
                    return new SupplierResult("Cogeferm", barcode, true, reference);
                }
            }
            
            return new SupplierResult("Cogeferm", barcode, true, "Réf: " + barcode);
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching Cogeferm: " + e.getMessage());
            return new SupplierResult("Cogeferm", barcode, false, null);
        }
    }
    
    /**
     * Search on Häfele website
     */
    private static SupplierResult searchHafele(String barcode) {
        try {
            String url = HAFELE_SEARCH_URLS[0] + barcode;
            Document doc = fetchDocument(url);
            
            if (doc != null) {
                Elements productElements = doc.select("div.product, .product-tile, .article-item");
                
                if (!productElements.isEmpty()) {
                    Element firstProduct = productElements.first();
                    String reference = extractReference(firstProduct, ".article-number, .item-no, .product-code");
                    return new SupplierResult("Häfele", barcode, true, reference);
                }
            }
            
            return new SupplierResult("Häfele", barcode, true, "Réf: " + barcode);
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching Häfele: " + e.getMessage());
            return new SupplierResult("Häfele", barcode, false, null);
        }
    }
    
    /**
     * Search on Foussier website
     */
    private static SupplierResult searchFoussier(String barcode) {
        try {
            String url = FOUSSIER_SEARCH_URLS[0] + barcode;
            Document doc = fetchDocument(url);
            
            if (doc != null) {
                Elements productElements = doc.select("div.product, .product-item, .fiche-article");
                
                if (!productElements.isEmpty()) {
                    Element firstProduct = productElements.first();
                    String reference = extractReference(firstProduct, ".reference, .code, .num-article");
                    return new SupplierResult("Foussier", barcode, true, reference);
                }
            }
            
            return new SupplierResult("Foussier", barcode, true, "Réf: " + barcode);
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching Foussier: " + e.getMessage());
            return new SupplierResult("Foussier", barcode, false, null);
        }
    }
    
    /**
     * Fetch HTML document from URL
     */
    private static Document fetchDocument(String url) throws IOException {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL: " + url + " - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extract reference from element using multiple selectors
     */
    private static String extractReference(Element element, String selectors) {
        String[] selectorArray = selectors.split(",");
        
        for (String selector : selectorArray) {
            Elements refElements = element.select(selector.trim());
            if (!refElements.isEmpty()) {
                return refElements.first().text().trim();
            }
        }
        
        return null;
    }
    
    /**
     * Normalize barcode (remove spaces, dashes, etc.)
     */
    private static String normalizeBarcode(String barcode) {
        if (barcode == null) return "";
        return barcode.trim().replaceAll("[^0-9]", "");
    }
    
    /**
     * Get fallback results when scraping fails
     */
    private static List<SupplierResult> getFallbackResults(String barcode) {
        List<SupplierResult> results = new ArrayList<>();
        results.add(new SupplierResult("Legallais", barcode, false, "Indisponible"));
        results.add(new SupplierResult("Trenois Descamps", barcode, false, "Indisponible"));
        results.add(new SupplierResult("Cogeferm", barcode, false, "Indisponible"));
        results.add(new SupplierResult("Häfele", barcode, false, "Indisponible"));
        results.add(new SupplierResult("Foussier", barcode, false, "Indisponible"));
        return results;
    }
    
    /**
     * Check if a product exists on a supplier website
     */
    public static boolean checkProductExists(String supplier, String barcode) {
        try {
            SupplierResult result = searchSupplier(supplier, barcode);
            return result != null && result.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Search a specific supplier
     */
    private static SupplierResult searchSupplier(String supplier, String barcode) {
        switch (supplier.toLowerCase()) {
            case "legallais":
                return searchLegallais(barcode);
            case "trenois":
            case "trenois descamps":
                return searchTrenoisDescamps(barcode);
            case "cogeferm":
                return searchCogeferm(barcode);
            case "hafele":
            case "häfele":
                return searchHafele(barcode);
            case "foussier":
                return searchFoussier(barcode);
            default:
                return new SupplierResult(supplier, barcode, false, null);
        }
    }
}
