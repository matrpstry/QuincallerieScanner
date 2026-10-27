package com.example.quincalleriescanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Scraper for extracting product data from supplier websites
 * This class scrapes product catalogs and stores them in the local database
 */
public class SupplierScraper {
    
    private static final String TAG = "SupplierScraper";
    
    // User agent for requests
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    // Timeout for requests (milliseconds)
    private static final int TIMEOUT_MS = 60000;
    
    // Number of threads for parallel scraping
    private static final int NUM_THREADS = 3;
    
    // Supplier configuration
    private static final Map<String, SupplierConfig> SUPPLIERS = new HashMap<>();
    
    static {
        // Legallais configuration
        SUPPLIERS.put("Legallais", new SupplierConfig(
                "https://www.legallais.com",
                "Legallais",
                "/recherche?query=",
                new String[]{"div.product-item", "li.product", "div.product-card"},
                new String[]{"data-sku", ".sku", ".reference", ".product-code", ".code-product"},
                new String[]{".product-name", ".name", "h3", "h2"},
                new String[]{".price", ".product-price", ".prix"},
                new String[]{".stock", ".availability", ".dispo"}
        ));
        
        // Trenois Descamps configuration
        SUPPLIERS.put("Trenois Descamps", new SupplierConfig(
                "https://www.trenois-descamps.com",
                "Trenois Descamps",
                "/recherche?search=",
                new String[]{"div.product", ".product-item", ".item-product", "article.product"},
                new String[]{".ref", ".reference", ".product-code", ".code-art"},
                new String[]{".product-title", ".name", "h3", "h2"},
                new String[]{".price", ".product-price", ".prix"},
                new String[]{".stock", ".availability", ".dispo"}
        ));
        
        // Cogeferm configuration
        SUPPLIERS.put("Cogeferm", new SupplierConfig(
                "https://www.cogeferm.com",
                "Cogeferm",
                "/recherche?controller=search&s=",
                new String[]{"div.product", ".product-box", ".article", ".fiche-article"},
                new String[]{".code", ".ref", ".product-ref", ".reference"},
                new String[]{".product-name", ".name", "h3", "h2"},
                new String[]{".price", ".product-price", ".prix"},
                new String[]{".stock", ".availability", ".dispo"}
        ));
        
        // Häfele configuration
        SUPPLIERS.put("Häfele", new SupplierConfig(
                "https://www.hafele.fr",
                "Häfele",
                "/fr-fr/search?query=",
                new String[]{"div.product", ".product-tile", ".article-item", ".product-card"},
                new String[]{".article-number", ".item-no", ".product-code", ".num-article"},
                new String[]{".product-name", ".name", "h3", "h2"},
                new String[]{".price", ".product-price", ".prix"},
                new String[]{".stock", ".availability", ".dispo"}
        ));
        
        // Foussier configuration
        SUPPLIERS.put("Foussier", new SupplierConfig(
                "https://www.foussier.com",
                "Foussier",
                "/recherche?search=",
                new String[]{"div.product", ".product-item", ".fiche-article", ".article"},
                new String[]{".reference", ".code", ".num-article", ".ref"},
                new String[]{".product-name", ".name", "h3", "h2"},
                new String[]{".price", ".product-price", ".prix"},
                new String[]{".stock", ".availability", ".dispo"}
        ));
    }
    
    private Context context;
    private DatabaseHelper dbHelper;
    private ScrapeListener listener;
    
    // Interface for scrape progress updates
    public interface ScrapeListener {
        void onScrapeStarted(String supplier);
        void onScrapeProgress(String supplier, int productsFound, int totalPages);
        void onScrapeCompleted(String supplier, int productsAdded);
        void onScrapeError(String supplier, String error);
        void onAllScrapingCompleted(int totalProducts);
    }
    
    // Supplier configuration class
    private static class SupplierConfig {
        String baseUrl;
        String supplierName;
        String searchPath;
        String[] productSelectors;
        String[] referenceSelectors;
        String[] nameSelectors;
        String[] priceSelectors;
        String[] stockSelectors;
        
        public SupplierConfig(String baseUrl, String supplierName, String searchPath, 
                            String[] productSelectors, String[] referenceSelectors,
                            String[] nameSelectors, String[] priceSelectors, String[] stockSelectors) {
            this.baseUrl = baseUrl;
            this.supplierName = supplierName;
            this.searchPath = searchPath;
            this.productSelectors = productSelectors;
            this.referenceSelectors = referenceSelectors;
            this.nameSelectors = nameSelectors;
            this.priceSelectors = priceSelectors;
            this.stockSelectors = stockSelectors;
        }
    }
    
    public SupplierScraper(Context context, ScrapeListener listener) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.listener = listener;
    }
    
    /**
     * Scrape all suppliers and populate the database
     */
    public void scrapeAllSuppliers() {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        List<Future<?>> futures = new ArrayList<>();
        
        for (Map.Entry<String, SupplierConfig> entry : SUPPLIERS.entrySet()) {
            String supplierName = entry.getKey();
            SupplierConfig config = entry.getValue();
            
            futures.add(executor.submit(() -> {
                try {
                    if (listener != null) {
                        ((ScrapeListener) listener).onScrapeStarted(supplierName);
                    }
                    
                    int productsAdded = scrapeSupplier(config);
                    
                    if (listener != null) {
                        ((ScrapeListener) listener).onScrapeCompleted(supplierName, productsAdded);
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        ((ScrapeListener) listener).onScrapeError(supplierName, e.getMessage());
                    }
                }
            }));
        }
        
        // Shutdown executor when all tasks are done
        executor.shutdown();
    }
    
    /**
     * Scrape a single supplier
     */
    private int scrapeSupplier(SupplierConfig config) {
        int productsAdded = 0;
        
        try {
            // Get the supplier ID from database
            int supplierId = dbHelper.getSupplierId(config.supplierName);
            if (supplierId == -1) {
                Log.e(TAG, "Supplier " + config.supplierName + " not found in database");
                return 0;
            }
            
            // Try to find sitemap or product listing pages
            List<String> productUrls = findProductPages(config);
            
            // Scrape each product page
            for (String url : productUrls) {
                try {
                    Document doc = fetchDocument(url);
                    if (doc != null) {
                        List<ProductData> products = extractProductsFromPage(doc, config);
                        for (ProductData product : products) {
                            if (product.ean != null && !product.ean.isEmpty()) {
                                // Check if product already exists
                                if (!dbHelper.contains(product.ean)) {
                                    long productId = dbHelper.insertProduct(
                                            product.ean, 
                                            product.name, 
                                            product.category, 
                                            product.brand, 
                                            product.description);
                                    
                                    dbHelper.insertProductSupplier(
                                            productId, 
                                            supplierId, 
                                            product.reference, 
                                            product.available, 
                                            product.price, 
                                            product.stock);
                                    productsAdded++;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error scraping page " + url + ": " + e.getMessage());
                }
                
                // Limit the number of pages to scrape (for demo)
                if (productsAdded >= 500) {
                    break;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error scraping supplier " + config.supplierName + ": " + e.getMessage());
        }
        
        return productsAdded;
    }
    
    /**
     * Find product listing pages for a supplier
     */
    private List<String> findProductPages(SupplierConfig config) {
        List<String> urls = new ArrayList<>();
        
        try {
            // Try sitemap
            try {
                Document sitemap = fetchDocument(config.baseUrl + "/sitemap.xml");
                if (sitemap != null) {
                    Elements links = sitemap.select("loc");
                    for (Element link : links) {
                        String url = link.text();
                        if (url.contains("/produit/" ) || url.contains("/product/") || 
                            url.contains("/article/") || url.contains("/fiche/")) {
                            urls.add(url);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "No sitemap found for " + config.supplierName);
            }
            
            // Try common product listing pages
            String[] commonPaths = {
                    "/produits",
                    "/products", 
                    "/catalogue",
                    "/catalog",
                    "/gamme",
                    "/rayon",
                    "/category",
                    "/cat"
            };
            
            for (String path : commonPaths) {
                try {
                    Document doc = fetchDocument(config.baseUrl + path);
                    if (doc != null) {
                        Elements productLinks = doc.select("a[href*=/produit/], a[href*=/product/], a[href*=/article/], a[href*=/fiche/]");
                        for (Element link : productLinks) {
                            String href = link.attr("href");
                            if (!href.startsWith("http")) {
                                href = config.baseUrl + (href.startsWith("/") ? "" : "/") + href;
                            }
                            if (!urls.contains(href)) {
                                urls.add(href);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            
            // Limit the number of URLs to scrape
            if (urls.size() > 100) {
                urls = urls.subList(0, 100);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error finding product pages: " + e.getMessage());
        }
        
        return urls;
    }
    
    /**
     * Extract products from a page
     */
    private List<ProductData> extractProductsFromPage(Document doc, SupplierConfig config) {
        List<ProductData> products = new ArrayList<>();
        
        try {
            // Try each product selector
            for (String selector : config.productSelectors) {
                Elements productElements = doc.select(selector);
                if (!productElements.isEmpty()) {
                    for (Element productElement : productElements) {
                        ProductData product = new ProductData();
                        
                        // Extract EAN/barcode
                        product.ean = extractText(productElement, config.referenceSelectors);
                        if (product.ean == null || product.ean.isEmpty()) {
                            // Try to find EAN in other attributes
                            product.ean = extractEANFromElement(productElement);
                        }
                        
                        // Extract reference
                        product.reference = extractText(productElement, config.referenceSelectors);
                        
                        // Extract name
                        product.name = extractText(productElement, config.nameSelectors);
                        
                        // Extract price
                        String priceText = extractText(productElement, config.priceSelectors);
                        if (priceText != null) {
                            try {
                                product.price = Float.parseFloat(priceText.replaceAll("[^0-9.]", ""));
                            } catch (NumberFormatException e) {
                                product.price = 0;
                            }
                        }
                        
                        // Extract stock/availability
                        String stockText = extractText(productElement, config.stockSelectors);
                        if (stockText != null) {
                            product.available = !stockText.toLowerCase().contains("indisponible") && 
                                               !stockText.toLowerCase().contains("rupture") &&
                                               !stockText.toLowerCase().contains("non dispo");
                        } else {
                            product.available = true; // Assume available if not specified
                        }
                        
                        // Extract category from page URL or title
                        product.category = extractCategory(doc);
                        
                        // Extract brand
                        product.brand = extractBrand(doc);
                        
                        if (product.name != null || product.ean != null) {
                            products.add(product);
                        }
                    }
                    break; // Found products with this selector
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Extract text from element using multiple selectors
     */
    private String extractText(Element element, String[] selectors) {
        for (String selector : selectors) {
            Elements elements = element.select(selector);
            if (!elements.isEmpty()) {
                return elements.first().text().trim();
            }
        }
        return null;
    }
    
    /**
     * Extract EAN from element (try data attributes, hidden fields, etc.)
     */
    private String extractEANFromElement(Element element) {
        // Try data-ean attribute
        String ean = element.attr("data-ean");
        if (ean != null && !ean.isEmpty()) return ean;
        
        // Try data-barcode
        ean = element.attr("data-barcode");
        if (ean != null && !ean.isEmpty()) return ean;
        
        // Try data-product-code
        ean = element.attr("data-product-code");
        if (ean != null && !ean.isEmpty()) return ean;
        
        // Try hidden input with ean/barcode
        Elements inputs = element.select("input[type=hidden]");
        for (Element input : inputs) {
            String name = input.attr("name").toLowerCase();
            if (name.contains("ean") || name.contains("barcode") || name.contains("code")) {
                return input.val();
            }
        }
        
        return null;
    }
    
    /**
     * Extract category from page
     */
    private String extractCategory(Document doc) {
        // Try breadcrumb
        Elements breadcrumbs = doc.select(".breadcrumb, .fil-ariane, .nav-path");
        if (!breadcrumbs.isEmpty()) {
            Element breadcrumb = breadcrumbs.first();
            Elements items = breadcrumb.select("li, span");
            if (!items.isEmpty()) {
                return items.get(items.size() - 2).text().trim();
            }
        }
        
        // Try page title
        Elements titles = doc.select("h1, title");
        if (!titles.isEmpty()) {
            return titles.first().text().trim();
        }
        
        return "Inconnu";
    }
    
    /**
     * Extract brand from page
     */
    private String extractBrand(Document doc) {
        Elements brands = doc.select(".brand, .marque, .fabricant");
        if (!brands.isEmpty()) {
            return brands.first().text().trim();
        }
        return "Générique";
    }
    
    /**
     * Fetch HTML document from URL
     */
    private Document fetchDocument(String url) throws IOException {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL: " + url + " - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Product data class
     */
    private static class ProductData {
        String ean;
        String reference;
        String name;
        String category;
        String brand;
        String description;
        float price;
        int stock;
        boolean available;
    }
    
    /**
     * Start scraping in background
     */
    public void startScraping() {
        new AsyncTask<Void, String, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                scrapeAllSuppliers();
                return dbHelper.getProductCount();
            }
            
            @Override
            protected void onPostExecute(Integer totalProducts) {
                if (listener != null) {
                    listener.onAllScrapingCompleted(totalProducts);
                }
            }
        }.execute();
    }
    
    /**
     * Scrape a single product by EAN from all suppliers
     */
    public List<SupplierResult> scrapeProductByEAN(String ean) {
        List<SupplierResult> results = new ArrayList<>();
        
        for (Map.Entry<String, SupplierConfig> entry : SUPPLIERS.entrySet()) {
            String supplierName = entry.getKey();
            SupplierConfig config = entry.getValue();
            
            try {
                // Search for the EAN on the supplier's website
                String searchUrl = config.baseUrl + config.searchPath + ean;
                Document doc = fetchDocument(searchUrl);
                
                if (doc != null) {
                    // Check if product is found
                    boolean found = false;
                    String reference = null;
                    boolean available = true;
                    
                    // Try to find product in search results
                    for (String selector : config.productSelectors) {
                        Elements products = doc.select(selector);
                        if (!products.isEmpty()) {
                            Element product = products.first();
                            reference = extractText(product, config.referenceSelectors);
                            found = true;
                            break;
                        }
                    }
                    
                    if (found) {
                        results.add(new SupplierResult(supplierName, ean, true, reference));
                    } else {
                        // Check for "no results" message
                        Elements noResults = doc.select(".no-results, .empty, .not-found");
                        if (!noResults.isEmpty()) {
                            results.add(new SupplierResult(supplierName, ean, false, null));
                        } else {
                            // Assume it might be available
                            results.add(new SupplierResult(supplierName, ean, true, "Réf: " + ean));
                        }
                    }
                } else {
                    results.add(new SupplierResult(supplierName, ean, false, null));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error scraping " + supplierName + " for EAN " + ean + ": " + e.getMessage());
                results.add(new SupplierResult(supplierName, ean, false, null));
            }
        }
        
        return results;
    }
}
