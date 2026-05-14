package com.gcepapers.app.ui.downloads;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.databinding.ItemDownloadedFileBinding;
import com.gcepapers.app.util.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for listing downloaded PDF files in the Downloads Manager screen.
 */
public class DownloadedFilesAdapter extends ListAdapter<DownloadedFile, DownloadedFilesAdapter.ViewHolder> {

    public interface OnOpenListener { void onOpen(DownloadedFile file); }
    public interface OnDeleteListener { void onDelete(DownloadedFile file); }

    private final OnOpenListener openListener;
    private final OnDeleteListener deleteListener;

    public DownloadedFilesAdapter(OnOpenListener open, OnDeleteListener delete) {
        super(DIFF_CALLBACK);
        this.openListener = open;
        this.deleteListener = delete;
    }

    private static final DiffUtil.ItemCallback<DownloadedFile> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<DownloadedFile>() {
            @Override
            public boolean areItemsTheSame(@NonNull DownloadedFile a, @NonNull DownloadedFile b) {
                return a.getUrl().equals(b.getUrl());
            }

            @Override
            public boolean areContentsTheSame(@NonNull DownloadedFile a, @NonNull DownloadedFile b) {
                return a.getUrl().equals(b.getUrl())
                    && a.getTitle().equals(b.getTitle())
                    && a.getFileSize() == b.getFileSize();
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDownloadedFileBinding binding = ItemDownloadedFileBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDownloadedFileBinding binding;

        ViewHolder(ItemDownloadedFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DownloadedFile file) {
            binding.fileName.setText(FileUtils.stripPdfExtensionForDisplay(file.getTitle()));
            binding.fileSize.setText(FileUtils.formatFileSize(file.getFileSize()));

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            binding.downloadDate.setText(sdf.format(new Date(file.getDownloadTime())));

            binding.openButton.setOnClickListener(v -> {
                if (openListener != null) openListener.onOpen(file);
            });

            binding.deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(file);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (openListener != null) openListener.onOpen(file);
            });
        }
    }
}
