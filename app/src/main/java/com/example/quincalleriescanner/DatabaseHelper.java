package com.example.quincalleriescanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for storing supplier products locally
 * This allows for offline searching and fast results
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = "DatabaseHelper";
    
    // Database version
    private static final int DATABASE_VERSION = 1;
    
    // Database name
    private static final String DATABASE_NAME = "QuincallerieProducts.db";
    
    // Table names
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_SUPPLIERS = "suppliers";
    public static final String TABLE_PRODUCT_SUPPLIER = "product_supplier";
    
    // Column names for products table
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EAN = "ean";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_DESCRIPTION = "description";
    
    // Column names for suppliers table
    public static final String COLUMN_SUPPLIER_ID = "_id";
    public static final String COLUMN_SUPPLIER_NAME = "name";
    public static final String COLUMN_SUPPLIER_URL = "url";
    public static final String COLUMN_SUPPLIER_LOGO = "logo_url";
    
    // Column names for product_supplier table (join table)
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_SUPPLIER_REF = "supplier_reference";
    public static final String COLUMN_AVAILABLE = "available";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_STOCK = "stock";
    public static final String COLUMN_LAST_UPDATED = "last_updated";
    
    // SQL for creating tables
    private static final String CREATE_TABLE_SUPPLIERS = 
            "CREATE TABLE " + TABLE_SUPPLIERS + " (" +
                    COLUMN_SUPPLIER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SUPPLIER_NAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_SUPPLIER_URL + " TEXT, " +
                    COLUMN_SUPPLIER_LOGO + " TEXT)";
    
    private static final String CREATE_TABLE_PRODUCTS = 
            "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EAN + " TEXT UNIQUE, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_BRAND + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT)";
    
    private static final String CREATE_TABLE_PRODUCT_SUPPLIER = 
            "CREATE TABLE " + TABLE_PRODUCT_SUPPLIER + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_ID + " INTEGER, " +
                    COLUMN_SUPPLIER_ID + " INTEGER, " +
                    COLUMN_SUPPLIER_REF + " TEXT, " +
                    COLUMN_AVAILABLE + " INTEGER DEFAULT 0, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_STOCK + " INTEGER, " +
                    COLUMN_LAST_UPDATED + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COLUMN_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_SUPPLIER_ID + ") REFERENCES " + TABLE_SUPPLIERS + "(" + COLUMN_SUPPLIER_ID + "))";
    
    // Indexes for faster searching
    private static final String CREATE_INDEX_EAN = 
            "CREATE INDEX idx_ean ON " + TABLE_PRODUCTS + "(" + COLUMN_EAN + ")";
    
    private static final String CREATE_INDEX_CATEGORY = 
            "CREATE INDEX idx_category ON " + TABLE_PRODUCTS + "(" + COLUMN_CATEGORY + ")";
    
    private static final String CREATE_INDEX_PRODUCT_SUPPLIER = 
            "CREATE INDEX idx_product_supplier ON " + TABLE_PRODUCT_SUPPLIER + 
            "(" + COLUMN_PRODUCT_ID + ", " + COLUMN_SUPPLIER_ID + ")";
    
    private Context context;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(CREATE_TABLE_SUPPLIERS);
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_PRODUCT_SUPPLIER);
        
        // Create indexes
        db.execSQL(CREATE_INDEX_EAN);
        db.execSQL(CREATE_INDEX_CATEGORY);
        db.execSQL(CREATE_INDEX_PRODUCT_SUPPLIER);
        
        // Insert default suppliers
        insertDefaultSuppliers(db);
        
        // Insert sample products (will be replaced by scraper data)
        insertSampleProducts(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_SUPPLIER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPLIERS);
        
        // Recreate tables
        onCreate(db);
    }
    
    /**
     * Insert default suppliers
     */
    private void insertDefaultSuppliers(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        
        // Legallais
        values.put(COLUMN_SUPPLIER_NAME, "Legallais");
        values.put(COLUMN_SUPPLIER_URL, "https://www.legallais.com");
        values.put(COLUMN_SUPPLIER_LOGO, "");
        db.insert(TABLE_SUPPLIERS, null, values);
        
        // Trenois Descamps
        values.clear();
        values.put(COLUMN_SUPPLIER_NAME, "Trenois Descamps");
        values.put(COLUMN_SUPPLIER_URL, "https://www.trenois-descamps.com");
        values.put(COLUMN_SUPPLIER_LOGO, "");
        db.insert(TABLE_SUPPLIERS, null, values);
        
        // Cogeferm
        values.clear();
        values.put(COLUMN_SUPPLIER_NAME, "Cogeferm");
        values.put(COLUMN_SUPPLIER_URL, "https://www.cogeferm.com");
        values.put(COLUMN_SUPPLIER_LOGO, "");
        db.insert(TABLE_SUPPLIERS, null, values);
        
        // Häfele
        values.clear();
        values.put(COLUMN_SUPPLIER_NAME, "Häfele");
        values.put(COLUMN_SUPPLIER_URL, "https://www.hafele.fr");
        values.put(COLUMN_SUPPLIER_LOGO, "");
        db.insert(TABLE_SUPPLIERS, null, values);
        
        // Foussier
        values.clear();
        values.put(COLUMN_SUPPLIER_NAME, "Foussier");
        values.put(COLUMN_SUPPLIER_URL, "https://www.foussier.com");
        values.put(COLUMN_SUPPLIER_LOGO, "");
        db.insert(TABLE_SUPPLIERS, null, values);
    }
    
    /**
     * Insert sample products for initial testing
     */
    private void insertSampleProducts(SQLiteDatabase db) {
        // This will be replaced by actual scraper data
        // For now, insert a few sample products
        long productId = insertProduct(db, "3000000000001", "Vis à bois 3x20", "Vis et fixations", "Generic", "Vis à bois inox");
        insertProductSupplier(db, productId, 1, "VIS-BOIS-3x20", true, 0.50f, 100);
        insertProductSupplier(db, productId, 2, "TD-VIS-001", true, 0.45f, 50);
        insertProductSupplier(db, productId, 3, "CF-VIS-3x20", true, 0.48f, 75);
        insertProductSupplier(db, productId, 5, "FV-3x20", true, 0.52f, 25);
        
        productId = insertProduct(db, "3000000000002", "Vis à bois 4x30", "Vis et fixations", "Generic", "Vis à bois galvanisé");
        insertProductSupplier(db, productId, 1, "VIS-BOIS-4x30", true, 0.60f, 100);
        insertProductSupplier(db, productId, 2, "TD-VIS-002", true, 0.55f, 50);
        insertProductSupplier(db, productId, 3, "CF-VIS-4x30", true, 0.58f, 75);
        insertProductSupplier(db, productId, 4, "HF-VIS-4x30", true, 0.62f, 25);
    }
    
    /**
     * Insert a product into the database
     */
    public long insertProduct(String ean, String name, String category, String brand, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_EAN, ean);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_BRAND, brand);
        values.put(COLUMN_DESCRIPTION, description);
        
        return db.insert(TABLE_PRODUCTS, null, values);
    }
    
    /**
     * Insert a product into the database (with db parameter)
     */
    private long insertProduct(SQLiteDatabase db, String ean, String name, String category, String brand, String description) {
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_EAN, ean);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_BRAND, brand);
        values.put(COLUMN_DESCRIPTION, description);
        
        return db.insert(TABLE_PRODUCTS, null, values);
    }
    
    /**
     * Insert product-supplier relationship
     */
    private void insertProductSupplier(SQLiteDatabase db, long productId, int supplierId, String reference, boolean available, float price, int stock) {
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_PRODUCT_ID, productId);
        values.put(COLUMN_SUPPLIER_ID, supplierId);
        values.put(COLUMN_SUPPLIER_REF, reference);
        values.put(COLUMN_AVAILABLE, available ? 1 : 0);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_STOCK, stock);
        values.put(COLUMN_LAST_UPDATED, System.currentTimeMillis());
        
        db.insert(TABLE_PRODUCT_SUPPLIER, null, values);
    }
    
    /**
     * Insert product-supplier relationship (public method for scraper)
     */
    public void insertProductSupplier(long productId, int supplierId, String reference, boolean available, float price, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        insertProductSupplier(db, productId, supplierId, reference, available, price, stock);
    }
    
    /**
     * Check if a product with given EAN exists
     */
    public boolean contains(String ean) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, 
                new String[]{COLUMN_ID}, 
                COLUMN_EAN + "=?", 
                new String[]{ean}, 
                null, null, null);
        
        if (cursor != null) {
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        }
        return false;
    }
    
    /**
     * Get supplier ID by name
     */
    public int getSupplierId(String supplierName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUPPLIERS, 
                new String[]{COLUMN_SUPPLIER_ID}, 
                COLUMN_SUPPLIER_NAME + "=?", 
                new String[]{supplierName}, 
                null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        
        return -1;
    }
    
    /**
     * Get all suppliers
     */
    public List<String> getAllSuppliers() {
        List<String> suppliers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_SUPPLIERS, 
                new String[]{COLUMN_SUPPLIER_NAME}, 
                null, null, null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                suppliers.add(cursor.getString(0));
            }
            cursor.close();
        }
        
        return suppliers;
    }
    
    /**
     * Search for a product by EAN/barcode
     */
    public List<SupplierResult> searchByEAN(String ean) {
        List<SupplierResult> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Normalize EAN (remove non-digit characters)
        String normalizedEan = ean != null ? ean.replaceAll("[^0-9]", "") : "";
        
        // Try exact match
        Cursor productCursor = db.query(TABLE_PRODUCTS, 
                new String[]{COLUMN_ID, COLUMN_EAN, COLUMN_NAME}, 
                COLUMN_EAN + "=?", 
                new String[]{normalizedEan}, 
                null, null, null);
        
        if (productCursor != null && productCursor.moveToFirst()) {
            long productId = productCursor.getLong(0);
            String productName = productCursor.getString(2);
            productCursor.close();
            
            // Get all suppliers for this product
            Cursor supplierCursor = db.query(TABLE_PRODUCT_SUPPLIER + " s JOIN " + TABLE_SUPPLIERS + " p ON s." + COLUMN_SUPPLIER_ID + "=p." + COLUMN_SUPPLIER_ID,
                    new String[]{"p." + COLUMN_SUPPLIER_NAME, "s." + COLUMN_SUPPLIER_REF, "s." + COLUMN_AVAILABLE, "s." + COLUMN_PRICE, "s." + COLUMN_STOCK},
                    "s." + COLUMN_PRODUCT_ID + "=?", 
                    new String[]{String.valueOf(productId)}, 
                    null, null, null);
            
            if (supplierCursor != null) {
                while (supplierCursor.moveToNext()) {
                    String supplierName = supplierCursor.getString(0);
                    String reference = supplierCursor.getString(1);
                    boolean available = supplierCursor.getInt(2) == 1;
                    float price = supplierCursor.getFloat(3);
                    int stock = supplierCursor.getInt(4);
                    
                    results.add(new SupplierResult(supplierName, normalizedEan, available, reference));
                }
                supplierCursor.close();
            }
        } else if (productCursor != null) {
            productCursor.close();
        }
        
        // If no exact match, try partial match (last 13 digits for EAN-13)
        if (results.isEmpty() && normalizedEan.length() >= 13) {
            String ean13 = normalizedEan.substring(normalizedEan.length() - 13);
            productCursor = db.query(TABLE_PRODUCTS, 
                    new String[]{COLUMN_ID, COLUMN_EAN, COLUMN_NAME}, 
                    COLUMN_EAN + "=?", 
                    new String[]{ean13}, 
                    null, null, null);
            
            if (productCursor != null && productCursor.moveToFirst()) {
                long productId = productCursor.getLong(0);
                productCursor.close();
                
                Cursor supplierCursor = db.query(TABLE_PRODUCT_SUPPLIER + " s JOIN " + TABLE_SUPPLIERS + " p ON s." + COLUMN_SUPPLIER_ID + "=p." + COLUMN_SUPPLIER_ID,
                        new String[]{"p." + COLUMN_SUPPLIER_NAME, "s." + COLUMN_SUPPLIER_REF, "s." + COLUMN_AVAILABLE},
                        "s." + COLUMN_PRODUCT_ID + "=?", 
                        new String[]{String.valueOf(productId)}, 
                        null, null, null);
                
                if (supplierCursor != null) {
                    while (supplierCursor.moveToNext()) {
                        String supplierName = supplierCursor.getString(0);
                        String reference = supplierCursor.getString(1);
                        boolean available = supplierCursor.getInt(2) == 1;
                        results.add(new SupplierResult(supplierName, ean13, available, reference));
                    }
                    supplierCursor.close();
                }
            } else if (productCursor != null) {
                productCursor.close();
            }
        }
        
        // If still no results, return all suppliers as not available
        if (results.isEmpty()) {
            for (String supplier : getAllSuppliers()) {
                results.add(new SupplierResult(supplier, normalizedEan, false, "Non référencé"));
            }
        }
        
        return results;
    }
    
    /**
     * Search products by name or description
     */
    public List<ProductInfo> searchByName(String query) {
        List<ProductInfo> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String searchQuery = "%" + query + "%";
        Cursor cursor = db.query(TABLE_PRODUCTS, 
                new String[]{COLUMN_ID, COLUMN_EAN, COLUMN_NAME, COLUMN_CATEGORY, COLUMN_BRAND}, 
                COLUMN_NAME + " LIKE ? OR " + COLUMN_DESCRIPTION + " LIKE ?", 
                new String[]{searchQuery, searchQuery}, 
                null, null, "10");
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ProductInfo info = new ProductInfo(
                        cursor.getString(1), // EAN
                        cursor.getString(2), // Name
                        cursor.getString(3), // Category
                        cursor.getString(4)  // Brand
                );
                products.add(info);
            }
            cursor.close();
        }
        
        return products;
    }
    
    /**
     * Get product count
     */
    public int getProductCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
        
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        
        return 0;
    }
    
    /**
     * Clear all products (useful for re-importing)
     */
    public void clearAllProducts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCT_SUPPLIER, null, null);
        db.delete(TABLE_PRODUCTS, null, null);
    }
    
    /**
     * Product info class for search results
     */
    public static class ProductInfo {
        private String ean;
        private String name;
        private String category;
        private String brand;
        
        public ProductInfo(String ean, String name, String category, String brand) {
            this.ean = ean;
            this.name = name;
            this.category = category;
            this.brand = brand;
        }
        
        public String getEan() { return ean; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getBrand() { return brand; }
    }
}
