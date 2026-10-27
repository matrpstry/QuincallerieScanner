package com.example.quincalleriescanner;

public class SupplierResult {
    private String supplierName;
    private String reference;
    private boolean isAvailable;
    private String supplierReference;

    public SupplierResult(String supplierName, String reference, boolean isAvailable, String supplierReference) {
        this.supplierName = supplierName;
        this.reference = reference;
        this.isAvailable = isAvailable;
        this.supplierReference = supplierReference;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getReference() {
        return reference;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getSupplierReference() {
        return supplierReference;
    }
}
