package com.example.quincalleriescanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local product database with known EAN/UPC codes for quincaillerie products
 * This provides instant results without requiring internet
 */
public class ProductDatabase {
    
    // Map: EAN/Barcode -> List of supplier results
    private static final Map<String, List<SupplierResult>> DATABASE = new HashMap<>();
    
    static {
        // ========== VIS ET FIXATIONS ==========
        
        // Vis à bois (different sizes)
        addProduct("3000000000001", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, false, true},
            new String[]{"VIS-BOIS-3x20", "TD-VIS-001", "CF-VIS-3x20", null, "FV-3x20"}
        ));
        
        addProduct("3000000000002", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, true, false},
            new String[]{"VIS-BOIS-4x30", "TD-VIS-002", "CF-VIS-4x30", "HF-VIS-4x30", null}
        ));
        
        addProduct("3000000000003", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, false, true, true},
            new String[]{"VIS-INOX-4x25", "TD-VIS-003", null, "HF-VIS-INOX-4x25", "FV-INOX-4x25"}
        ));
        
        // Vis à métaux
        addProduct("3000000000010", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, false, true, true, true},
            new String[]{"VIS-METAL-M5x16", null, "CF-VIS-M5x16", "HF-M5x16", "FV-M5x16"}
        ));
        
        // Écrous
        addProduct("3000000000020", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, false, false},
            new String[]{"ECROU-M6-GALV", "TD-EC-M6", "CF-EC-M6", null, null}
        ));
        
        addProduct("3000000000021", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, true, true},
            new String[]{"ECROU-M8-INOX", "TD-EC-M8", "CF-EC-M8", "HF-EC-M8", "FV-EC-M8"}
        ));
        
        // Rondelles
        addProduct("3000000000030", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, false, true, true},
            new String[]{"ROND-M6-PLAT", "TD-RD-M6", null, "HF-RD-M6", "FR-M6"}
        ));
        
        // ========== CHARNIERES ET FERRURES ==========
        
        // Charnières
        addProduct("3000000000040", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, false, true, true, true},
            new String[]{"CHAR-35-PAUM", null, "CF-CH-35", "HF-CH-35-S", "FCH-35"}
        ));
        
        addProduct("3000000000041", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, true, false},
            new String[]{"CHAR-40-PAUM", "TD-CH-40", "CF-CH-40", "HF-CH-40", null}
        ));
        
        // Fermetures
        addProduct("3000000000050", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, false, true, true},
            new String[]{"FERM-PORTE-60", "TD-FERM-60", null, "HF-FERM-60", "FF-60"}
        ));
        
        // ========== OUTILS ==========
        
        // Serre-joints
        addProduct("3000000000060", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, false, false, true},
            new String[]{"SJ-150mm", "TD-SJ-150", null, null, "FSJ-150"}
        ));
        
        // Niveaux
        addProduct("3000000000061", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, false, false},
            new String[]{"NIV-60cm", "TD-NIV-60", "CF-NIV-60", null, null}
        ));
        
        // ========== ELECTRICITE ==========
        
        // Interrupteurs
        addProduct("3000000000070", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, false, false},
            new String[]{"INT-LEGRAND", "TD-INT", "CF-INT", null, null}
        ));
        
        // Prises
        addProduct("3000000000071", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, false, true, true, true},
            new String[]{"PRISE-16A", null, "CF-PRISE-16A", "HF-PRISE", "FP-16A"}
        ));
        
        // ========== TEST CODES ==========
        
        // Test code 1
        addProduct("123456789012", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, true, true},
            new String[]{"TEST-LGL-001", "TEST-TD-001", "TEST-CF-001", "TEST-HF-001", "TEST-FS-001"}
        ));
        
        // Test code 2
        addProduct("1234567890", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, true, true},
            new String[]{"TEST-LGL-002", "TEST-TD-002", "TEST-CF-002", "TEST-HF-002", "TEST-FS-002"}
        ));
        
        // Test code 3
        addProduct("12345678", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, true, true},
            new String[]{"TEST-LGL-003", "TEST-TD-003", "TEST-CF-003", "TEST-HF-003", "TEST-FS-003"}
        ));
        
        // Test code 4
        addProduct("888888888888", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, true, true, true, true},
            new String[]{"TEST-LGL-004", "TEST-TD-004", "TEST-CF-004", "TEST-HF-004", "TEST-FS-004"}
        ));
        
        // ========== REAL PRODUCT CODES (examples) ==========
        // These are example EAN codes - replace with your actual product codes
        
        // Example: 3254871000000 (generic hardware product)
        addProduct("3254871000000", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{true, false, true, true, false},
            new String[]{"PROD-001", null, "CF-325487", "HF-325487", null}
        ));
        
        // Example: 3254871000001
        addProduct("3254871000001", createResults(
            new String[]{"Legallais", "Trenois Descamps", "Cogeferm", "Häfele", "Foussier"},
            new boolean[]{false, true, true, false, true},
            new String[]{null, "TD-325487-1", "CF-325487-1", null, "FS-325487-1"}
        ));
    }
    
    /**
     * Add a product to the database
     */
    private static void addProduct(String barcode, List<SupplierResult> results) {
        DATABASE.put(normalizeBarcode(barcode), results);
    }
    
    /**
     * Create supplier results from arrays
     */
    private static List<SupplierResult> createResults(String[] suppliers, boolean[] available, String[] references) {
        List<SupplierResult> results = new ArrayList<>();
        for (int i = 0; i < suppliers.length; i++) {
            String ref = i < references.length ? references[i] : null;
            results.add(new SupplierResult(suppliers[i], null, available[i], ref));
        }
        return results;
    }
    
    /**
     * Normalize barcode (remove non-digit characters)
     */
    private static String normalizeBarcode(String barcode) {
        if (barcode == null) return "";
        return barcode.trim().replaceAll("[^0-9]", "");
    }
    
    /**
     * Search for a barcode in the database
     */
    public static List<SupplierResult> search(String barcode) {
        String normalizedBarcode = normalizeBarcode(barcode);
        
        // Try exact match
        if (DATABASE.containsKey(normalizedBarcode)) {
            List<SupplierResult> results = DATABASE.get(normalizedBarcode);
            // Update barcode in results
            for (SupplierResult result : results) {
                result = new SupplierResult(result.getSupplierName(), normalizedBarcode, result.isAvailable(), result.getSupplierReference());
            }
            return results;
        }
        
        // Try with last 13 digits (EAN-13)
        if (normalizedBarcode.length() >= 13) {
            String ean13 = normalizedBarcode.substring(normalizedBarcode.length() - 13);
            if (DATABASE.containsKey(ean13)) {
                return DATABASE.get(ean13);
            }
        }
        
        // Try with last 8 digits
        if (normalizedBarcode.length() >= 8) {
            String shortCode = normalizedBarcode.substring(normalizedBarcode.length() - 8);
            if (DATABASE.containsKey(shortCode)) {
                return DATABASE.get(shortCode);
            }
        }
        
        // If not found, return default results (all not available)
        return getDefaultResults(normalizedBarcode);
    }
    
    /**
     * Get default results when product not found
     */
    private static List<SupplierResult> getDefaultResults(String barcode) {
        List<SupplierResult> results = new ArrayList<>();
        results.add(new SupplierResult("Legallais", barcode, false, "Non référencé"));
        results.add(new SupplierResult("Trenois Descamps", barcode, false, "Non référencé"));
        results.add(new SupplierResult("Cogeferm", barcode, false, "Non référencé"));
        results.add(new SupplierResult("Häfele", barcode, false, "Non référencé"));
        results.add(new SupplierResult("Foussier", barcode, false, "Non référencé"));
        return results;
    }
    
    /**
     * Add a custom product to the database at runtime
     */
    public static void addCustomProduct(String barcode, List<SupplierResult> results) {
        DATABASE.put(normalizeBarcode(barcode), results);
    }
    
    /**
     * Get the number of products in the database
     */
    public static int getProductCount() {
        return DATABASE.size();
    }
    
    /**
     * Check if a barcode exists in the database
     */
    public static boolean contains(String barcode) {
        return DATABASE.containsKey(normalizeBarcode(barcode));
    }
}
