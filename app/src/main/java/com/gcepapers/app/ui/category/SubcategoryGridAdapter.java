package com.gcepapers.app.ui.category;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gcepapers.app.R;
import com.gcepapers.app.data.model.Category;
import com.gcepapers.app.databinding.ItemCategoryGridBinding;

/**
 * Adapter for displaying subcategories in a 2-column grid inside CategoryActivity.
 */
public class SubcategoryGridAdapter extends ListAdapter<Category, SubcategoryGridAdapter.ViewHolder> {

    public interface OnSubcategoryClickListener {
        void onClick(Category category);
    }

    private final OnSubcategoryClickListener listener;

    public SubcategoryGridAdapter(OnSubcategoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<Category>() {
            @Override
            public boolean areItemsTheSame(@NonNull Category a, @NonNull Category b) {
                return a.getName() != null && a.getName().equals(b.getName());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Category a, @NonNull Category b) {
                return a.getName() != null && a.getName().equals(b.getName());
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryGridBinding binding = ItemCategoryGridBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryGridBinding binding;

        ViewHolder(ItemCategoryGridBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.categoryTitle.setText(category.getName());

            String iconUrl = category.getIcon();
            if (iconUrl != null && !iconUrl.isEmpty()) {
                Glide.with(binding.categoryIcon.getContext())
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_category_placeholder)
                    .error(R.drawable.ic_category_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(binding.categoryIcon);
            } else {
                binding.categoryIcon.setImageResource(R.drawable.ic_category_placeholder);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(category);
            });
        }
    }
}
