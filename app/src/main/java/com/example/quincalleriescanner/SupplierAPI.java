package com.example.quincalleriescanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SupplierAPI {
    
    // Base de données locale de produits connus (codes-barres -> références fournisseurs)
    // Format: code-barres -> Liste de résultats par fournisseur
    private static final Map<String, List<SupplierResult>> KNOWN_PRODUCTS = new HashMap<>();
    
    // Initialisation avec des exemples de produits de quincaillerie
    static {
        // Vis - EAN 3000000000001
        List<SupplierResult> visResults = new ArrayList<>();
        visResults.add(new SupplierResult("Legallais", "3000000000001", true, "VIS-INOX-M6x20"));
        visResults.add(new SupplierResult("Trenois Descamps", "3000000000001", true, "TD-VIS-001"));
        visResults.add(new SupplierResult("Cogeferm", "3000000000001", true, "CF3001"));
        visResults.add(new SupplierResult("Häfele", "3000000000001", false, null));
        visResults.add(new SupplierResult("Foussier", "3000000000001", true, "FV-001"));
        KNOWN_PRODUCTS.put("3000000000001", visResults);
        
        // Ecrou - EAN 3000000000002
        List<SupplierResult> ecrouResults = new ArrayList<>();
        ecrouResults.add(new SupplierResult("Legallais", "3000000000002", true, "ECROU-M6-GALV"));
        ecrouResults.add(new SupplierResult("Trenois Descamps", "3000000000002", true, "TD-EC-002"));
        ecrouResults.add(new SupplierResult("Cogeferm", "3000000000002", true, "CF3002"));
        ecrouResults.add(new SupplierResult("Häfele", "3000000000002", true, "HF-EC-M6"));
        ecrouResults.add(new SupplierResult("Foussier", "3000000000002", false, null));
        KNOWN_PRODUCTS.put("3000000000002", ecrouResults);
        
        // Rondelle - EAN 3000000000003
        List<SupplierResult> rondelleResults = new ArrayList<>();
        rondelleResults.add(new SupplierResult("Legallais", "3000000000003", true, "ROND-M6-INOX"));
        rondelleResults.add(new SupplierResult("Trenois Descamps", "3000000000003", true, "TD-RD-003"));
        rondelleResults.add(new SupplierResult("Cogeferm", "3000000000003", false, null));
        rondelleResults.add(new SupplierResult("Häfele", "3000000000003", true, "HF-RD-M6"));
        rondelleResults.add(new SupplierResult("Foussier", "3000000000003", true, "FR-003"));
        KNOWN_PRODUCTS.put("3000000000003", rondelleResults);
        
        // Charniere - EAN 3000000000004
        List<SupplierResult> charniereResults = new ArrayList<>();
        charniereResults.add(new SupplierResult("Legallais", "3000000000004", true, "CHAR-35mm"));
        charniereResults.add(new SupplierResult("Trenois Descamps", "3000000000004", false, null));
        charniereResults.add(new SupplierResult("Cogeferm", "3000000000004", true, "CF3004"));
        charniereResults.add(new SupplierResult("Häfele", "3000000000004", true, "HF-CH-35"));
        charniereResults.add(new SupplierResult("Foussier", "3000000000004", true, "FCH-35"));
        KNOWN_PRODUCTS.put("3000000000004", charniereResults);
        
        // Serre-joint - EAN 3000000000005
        List<SupplierResult> serreJointResults = new ArrayList<>();
        serreJointResults.add(new SupplierResult("Legallais", "3000000000005", true, "SJ-150mm"));
        serreJointResults.add(new SupplierResult("Trenois Descamps", "3000000000005", true, "TD-SJ-150"));
        serreJointResults.add(new SupplierResult("Cogeferm", "3000000000005", false, null));
        serreJointResults.add(new SupplierResult("Häfele", "3000000000005", false, null));
        serreJointResults.add(new SupplierResult("Foussier", "3000000000005", true, "FSJ-150"));
        KNOWN_PRODUCTS.put("3000000000005", serreJointResults);
        
        // Test codes for demo
        List<SupplierResult> testResults = new ArrayList<>();
        testResults.add(new SupplierResult("Legallais", "123456789012", true, "TEST-LGL"));
        testResults.add(new SupplierResult("Trenois Descamps", "123456789012", true, "TEST-TD"));
        testResults.add(new SupplierResult("Cogeferm", "123456789012", true, "TEST-CF"));
        testResults.add(new SupplierResult("Häfele", "123456789012", true, "TEST-HF"));
        testResults.add(new SupplierResult("Foussier", "123456789012", true, "TEST-FS"));
        KNOWN_PRODUCTS.put("123456789012", testResults);
        KNOWN_PRODUCTS.put("1234567890", testResults);
        KNOWN_PRODUCTS.put("12345678", testResults);
    }
    
    // Recherche dans la base de données locale
    public static List<SupplierResult> searchAllSuppliers(String barcode) {
        List<SupplierResult> results = new ArrayList<>();
        
        // Normaliser le code-barres (supprimer les espaces, majuscules)
        String normalizedBarcode = barcode != null ? barcode.trim().toUpperCase() : "";
        
        // Vérifier si on connaît ce code-barres
        if (KNOWN_PRODUCTS.containsKey(normalizedBarcode)) {
            return KNOWN_PRODUCTS.get(normalizedBarcode);
        }
        
        // Si le code-barres est très long (EAN-13), essayer avec les derniers chiffres
        if (normalizedBarcode.length() > 8) {
            String shortCode = normalizedBarcode.substring(normalizedBarcode.length() - 8);
            if (KNOWN_PRODUCTS.containsKey(shortCode)) {
                return KNOWN_PRODUCTS.get(shortCode);
            }
        }
        
        // Si pas trouvé, retourner des résultats par défaut (tous non disponibles)
        results.add(new SupplierResult("Legallais", normalizedBarcode, false, "Non référencé"));
        results.add(new SupplierResult("Trenois Descamps", normalizedBarcode, false, "Non référencé"));
        results.add(new SupplierResult("Cogeferm", normalizedBarcode, false, "Non référencé"));
        results.add(new SupplierResult("Häfele", normalizedBarcode, false, "Non référencé"));
        results.add(new SupplierResult("Foussier", normalizedBarcode, false, "Non référencé"));
        
        return results;
    }
    
    // Ajouter un produit manuellement (pour étendre la base de données)
    public static void addProduct(String barcode, List<SupplierResult> supplierResults) {
        KNOWN_PRODUCTS.put(barcode, supplierResults);
    }
    
    // Vérifier si un code-barres est connu
    public static boolean isKnownProduct(String barcode) {
        String normalizedBarcode = barcode != null ? barcode.trim().toUpperCase() : "";
        return KNOWN_PRODUCTS.containsKey(normalizedBarcode);
    }
    
    // Obtenir le nombre de produits dans la base
    public static int getProductCount() {
        return KNOWN_PRODUCTS.size();
    }
}
