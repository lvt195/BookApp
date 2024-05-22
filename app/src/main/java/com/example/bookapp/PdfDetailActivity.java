package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    String bookId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        loadBookDetail(bookId);

        //tang luot xem
        MyApplication.incrementBookCount(bookId);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadBookDetail(String bookId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadCount = ""+snapshot.child("downloadCount").getValue();
                        String url = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //format date
                        String date = MyApplication.formatTimeStamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(categoryId,binding.categoryTv);
                        MyApplication.loadBannerPdf(url,title,binding.pdfView,binding.progressBar);
                        MyApplication.loadPdfSize(url,title,binding.sizeTv);

                        binding.titleTv.setText(title);
                        binding.descriptionTv.setText(description);
                        binding.viewTv.setText(viewCount);
                        binding.downTv.setText(downloadCount);
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}