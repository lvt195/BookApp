package com.example.bookapp.filter;

import android.widget.Filter;

import com.example.bookapp.adapter.AdapterPdfAdmin;
import com.example.bookapp.adapter.AdapterPdfUser;
import com.example.bookapp.model.ModelPdf;

import java.util.ArrayList;
import android.widget.Filter;

public class FilterPdfUser extends Filter{
    ArrayList<ModelPdf> filterList;
    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }


    @Override
    protected Filter.FilterResults performFiltering(CharSequence constraint) {
        Filter.FilterResults results = new Filter.FilterResults();
        if(constraint != null && constraint.length() > 0){

            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filterModels = new ArrayList<>();

            for(int i = 0 ; i < filterList.size(); i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filterModels.add(filterList.get(i));
                }
            }

            results.count = filterModels.size();
            results.values = filterModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
        //ap dung filter
        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>) results.values;
        adapterPdfUser.notifyDataSetChanged();

    }
}
