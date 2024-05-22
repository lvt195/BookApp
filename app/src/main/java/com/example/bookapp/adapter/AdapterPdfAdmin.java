package com.example.bookapp.adapter;

import static com.example.bookapp.Constants.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.PdfDetailActivity;
import com.example.bookapp.PdfEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filter.FilterPdfAdmin;
import com.example.bookapp.model.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private final Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    private final ProgressDialog progressDialog;

    public static final String TAG = "PDF_ADMIN";


    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {

        //lay du lieu
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();

        String formatDate = MyApplication.formatTimeStamp(timestamp);


        //gan du lieu vao holder
        holder.titletv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formatDate);

        MyApplication.loadCategory(""+categoryId, holder.categoryTv);
        MyApplication.loadBannerPdf(""+pdfUrl,""+title,holder.pdfView,holder.progressBar);
        MyApplication.loadPdfSize(""+pdfUrl,""+title,holder.sizeTv);

        //bam vao hien option sua xoa
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(model, holder);
            }
        });

        //click vao cac item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookID", ""+pdfId);
                context.startActivity(intent);
            }
        });

    }

    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        String[] options = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Ooptions")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){// nhap vao edit
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            intent.putExtra("bookUrl", bookUrl);
                            intent.putExtra("bookTitle", bookTitle);
                        } else if (which == 1) {//nhap vao xoa
                            MyApplication.deleteBook(
                                context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle
                            );
                        }

                    }
                })
                .show();

    }



    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{


        PDFView pdfView;
        ProgressBar progressBar;
        TextView titletv, descriptionTv, categoryTv, sizeTv, dateTv;

        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titletv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
