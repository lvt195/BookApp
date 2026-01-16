package com.pluto.bookapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pluto.bookapp.MyApplication;
import com.pluto.bookapp.activities.PdfDetailActivity;
import com.pluto.bookapp.databinding.RowFavoriteBookBinding;
import com.pluto.bookapp.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.HolderPdfFavorite> {

    private static final String TAG = "ADAPTER_PDF_FAVORITE";
    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;

    private RowFavoriteBookBinding binding;


    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowFavoriteBookBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {

        ModelPdf model = pdfArrayList.get(position);

        Log.d(TAG, "cuon sach co url "+ model.getUrl());

        loadBookDetail(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);
            }
        });

        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context, model.getId());
            }
        });

    }

    private void loadBookDetail(ModelPdf model, HolderPdfFavorite holder) {
    
        String bookId = model.getId();
        Log.d(TAG, "loadBookDetail: Book detail in favorite by bookid: "+bookId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadCount = ""+snapshot.child("downloadCount").getValue();

                        Log.d(TAG, "url: "+ bookUrl);

                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setCategoryId(categoryId);
                        model.setDescription(description);
                        model.setUrl(bookUrl);
                        model.setTimestamp(Long.parseLong(timestamp));
                        model.setUid(uid);

                        String date = MyApplication.formatTimeStamp(Long.parseLong(timestamp));


                        MyApplication.loadCategory(""+categoryId, holder.categoryTv);
                        MyApplication.loadBannerPdf(""+bookUrl, ""+bookTitle, holder.pdfView, holder.progressBar, null);
                        MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle,holder.sizeTv);


                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(description);
                        holder.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavorite extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton removeFavBtn;
        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            removeFavBtn = binding.removeFavBtn;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
        }
    }
}
