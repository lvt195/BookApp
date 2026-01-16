package com.example.bookapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.adapter.AdapterComment;
import com.example.bookapp.databinding.ActivityPdfDetailBinding;
import com.example.bookapp.databinding.DialogCommentAddBinding;
import com.example.bookapp.model.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    String bookId, bookTitle, bookUrl;

    boolean isInFavorite = false;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        bookTitle = intent.getStringExtra("bookTitle");

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        if(firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }
        else {
            Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
        }

        loadBookDetail();

        loadComments();

        //tang luot xem
        MyApplication.incrementBookCount(bookId);

        //nut doc sach
        binding.readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                intent1.putExtra("bookTitle", bookTitle);
                startActivity(intent1);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


//        show, add comment
        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(PdfDetailActivity.this, "You are not logged in...", Toast.LENGTH_SHORT).show();
                }
                else {
                    addCommentDialog();
                }
            }
        });


    }

    private void loadComments() {

        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){

                            ModelComment model = ds.getValue(ModelComment.class);

                            commentArrayList.add(model);
                        }

                        //setup adapter
                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);
                        binding.commentsRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    String comment = "";

    private void addCommentDialog() {
        // thoi view vao dialog
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        // setup alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        //tao va show dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //click back thi thoat ra khio dialog add cmt
        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        //click them commment
        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                comment = commentAddBinding.commentEt.getText().toString().trim();

                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(PdfDetailActivity.this, "Enter your comment", Toast.LENGTH_SHORT).show();
                }
                else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });


    }

    private void addComment() {

        progressDialog.setMessage("Adding comment..");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("bookId",""+bookId);
        hashMap.put("timestamp",""+bookId);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //Books bookID Comments commentID commentData
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, "Failed to add comment: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private static final String TAG_DOWN="DOWNLOAD_TAG";
    

    private void loadBookDetail() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadCount = ""+snapshot.child("downloadCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();


                        //khi load duoc url roi thi se hien thi lai
                        binding.downBtn.setVisibility(View.VISIBLE);


                        //format date
                        String date = MyApplication.formatTimeStamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(categoryId,binding.categoryTv);
                        MyApplication.loadBannerPdf(bookUrl,bookUrl,binding.pdfView,binding.progressBar, binding.pageTv);
                        MyApplication.loadPdfSize(bookUrl,bookUrl,binding.sizeTv);

                        binding.titleTv.setText(bookTitle);
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

    private void checkIsFavorite(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInFavorite = snapshot.exists();
                        if(isInFavorite){
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
                            binding.favoriteBtn.setText("Remove Favorite");
                            binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);
                                }
                            });
                        }
                        else {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
                            binding.favoriteBtn.setText("Add Favorite");
                            binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MyApplication.addToFavorite(PdfDetailActivity.this, bookId);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}