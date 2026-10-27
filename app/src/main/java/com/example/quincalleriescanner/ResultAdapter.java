package com.example.quincalleriescanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {
    private List<SupplierResult> results;

    public ResultAdapter(List<SupplierResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        SupplierResult result = results.get(position);
        
        holder.supplierName.setText(result.getSupplierName());
        
        if (result.isAvailable()) {
            holder.statusText.setText("Disponible");
            holder.statusText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorAvailable));
        } else {
            holder.statusText.setText("Non disponible");
            holder.statusText.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorNotAvailable));
        }
        
        String referenceText = "Réf: ";
        if (result.getSupplierReference() != null && !result.getSupplierReference().isEmpty()) {
            referenceText += result.getSupplierReference();
        } else if (result.getReference() != null && !result.getReference().isEmpty()) {
            referenceText += result.getReference();
        } else {
            referenceText += "Non spécifiée";
        }
        holder.referenceText.setText(referenceText);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public void updateResults(List<SupplierResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView supplierName;
        TextView statusText;
        TextView referenceText;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            supplierName = itemView.findViewById(R.id.supplierName);
            statusText = itemView.findViewById(R.id.statusText);
            referenceText = itemView.findViewById(R.id.referenceText);
        }
    }
}
