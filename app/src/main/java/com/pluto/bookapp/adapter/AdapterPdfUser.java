package com.pluto.bookapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pluto.bookapp.MyApplication;
import com.pluto.bookapp.activities.PdfDetailActivity;
import com.pluto.bookapp.databinding.RowPdfUserBinding;
import com.pluto.bookapp.filter.FilterPdfUser;
import com.pluto.bookapp.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {


    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private FilterPdfUser filter;

    private RowPdfUserBinding binding;
    public static final String TAG = "ADAPTER_PDF_USER_TAG";


    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {

        ModelPdf model = pdfArrayList.get(position);
        String bookId = model.getId();
        String bookTitle = model.getTitle();
        String description = model.getDescription();
        String bookUrl = model.getUrl();
        String categoryId = model.getCategoryId();

        long timestamp = model.getTimestamp();

        String date = MyApplication.formatTimeStamp(timestamp);

        holder.titleTv.setText(bookTitle);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);


        MyApplication.loadBannerPdf(""+bookUrl, ""+bookTitle, holder.pdfView, holder.progressBar, null);

        MyApplication.loadCategory(""+categoryId, holder.categoryTv);

        MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle, holder.sizeTv);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", bookId);
                intent.putExtra("bookTitle", ""+bookTitle);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterPdfUser(filterList, this);
        }
        return filter;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder{


        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv, viewCountTv, downloadCountTv;
        PDFView pdfView;
        ProgressBar progressBar;


        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
