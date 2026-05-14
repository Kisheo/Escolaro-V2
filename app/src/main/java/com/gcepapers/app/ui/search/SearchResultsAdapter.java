package com.gcepapers.app.ui.search;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gcepapers.app.R;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.data.model.ContentItem;
import com.gcepapers.app.databinding.ItemSearchResultBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for search results. Shows both Category and ContentItem results.
 * Highlights the matched query in the title.
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    public interface OnResultClickListener {
        void onClick(Object result);
    }

    private List<Object> results = new ArrayList<>();
    private String query = "";
    private final OnResultClickListener listener;

    public SearchResultsAdapter(OnResultClickListener listener) {
        this.listener = listener;
    }

    public void submitResults(List<Object> newResults, String searchQuery) {
        this.results = newResults != null ? newResults : new ArrayList<>();
        this.query = searchQuery != null ? searchQuery : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchResultBinding binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(results.get(position));
    }

    @Override
    public int getItemCount() { return results.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSearchResultBinding binding;

        ViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Object item) {
            String title;
            String subtitle;
            int iconRes;

            if (item instanceof ContentItem) {
                ContentItem content = (ContentItem) item;
                title = content.getTitle() != null ? content.getTitle().trim() : "";
                subtitle = content.getCategoryPath() != null ? content.getCategoryPath() : "";
                iconRes = R.drawable.ic_pdf;
            } else if (item instanceof Category) {
                Category cat = (Category) item;
                title = cat.getName() != null ? cat.getName() : "";
                subtitle = cat.getPath() != null ? cat.getPath() : "";
                iconRes = R.drawable.ic_folder;
            } else {
                return;
            }

            binding.resultIcon.setImageResource(iconRes);
            binding.resultTitle.setText(highlightQuery(title, query));
            binding.resultSubtitle.setText(subtitle);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
        }

        private SpannableString highlightQuery(String text, String query) {
            if (query == null || query.isEmpty()) return new SpannableString(text);
            SpannableString spannable = new SpannableString(text);
            String lowerText = text.toLowerCase();
            String lowerQuery = query.toLowerCase();
            int start = lowerText.indexOf(lowerQuery);
            while (start >= 0) {
                int end = start + query.length();
                spannable.setSpan(new BackgroundColorSpan(
                    ContextCompat.getColor(binding.getRoot().getContext(), R.color.highlight_color)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = lowerText.indexOf(lowerQuery, end);
            }
            return spannable;
        }
    }
}
