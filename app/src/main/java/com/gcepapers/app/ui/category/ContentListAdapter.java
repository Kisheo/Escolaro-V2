package com.gcepapers.app.ui.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gcepapers.app.R;
import com.gcepapers.app.data.db.entity.DownloadedFile;
import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.databinding.ItemContentListBinding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter for displaying PDF content items in a vertical list.
 * Shows only the PDF title.
 * Row turns green + shows "Available Offline" if file is downloaded.
 */
public class ContentListAdapter extends ListAdapter<ContentItem, ContentListAdapter.ViewHolder> {

    public interface OnContentClickListener {
        void onClick(ContentItem item);
    }

    private final OnContentClickListener listener;
    private final Set<String> downloadedUrls = new HashSet<>();

    public ContentListAdapter(OnContentClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ContentItem> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<ContentItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ContentItem a, @NonNull ContentItem b) {
                return a.getUrl() != null && a.getUrl().equals(b.getUrl());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ContentItem a, @NonNull ContentItem b) {
                return a.getUrl() != null && a.getUrl().equals(b.getUrl())
                    && a.getTitle() != null && a.getTitle().equals(b.getTitle());
            }
        };

    public void updateDownloadedUrls(List<DownloadedFile> downloads) {
        downloadedUrls.clear();
        if (downloads != null) {
            for (DownloadedFile df : downloads) {
                downloadedUrls.add(df.getUrl());
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContentListBinding binding = ItemContentListBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemContentListBinding binding;

        ViewHolder(ItemContentListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ContentItem item) {
            binding.pdfTitle.setText(com.gcepapers.app.util.FileUtils.stripPdfExtensionForDisplay(item.getTitle()));

            boolean isDownloaded = downloadedUrls.contains(item.getUrl());
            if (isDownloaded) {
                binding.offlineBadge.setVisibility(View.VISIBLE);
                binding.getRoot().setBackgroundColor(
                    ContextCompat.getColor(binding.getRoot().getContext(),
                        R.color.downloaded_item_bg));
            } else {
                binding.offlineBadge.setVisibility(View.GONE);
                binding.getRoot().setBackgroundColor(
                    ContextCompat.getColor(binding.getRoot().getContext(),
                        android.R.color.transparent));
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
        }
    }
}
