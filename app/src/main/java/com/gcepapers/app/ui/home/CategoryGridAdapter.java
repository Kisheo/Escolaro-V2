package com.gcepapers.app.ui.home;

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
 * RecyclerView adapter for displaying categories in a 2-column grid.
 * Each card shows category icon + title only (no text-heavy content).
 */
public class CategoryGridAdapter extends ListAdapter<Category, CategoryGridAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final OnCategoryClickListener clickListener;

    public CategoryGridAdapter(OnCategoryClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<Category>() {
            @Override
            public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                return oldItem.getName() != null && oldItem.getName().equals(newItem.getName());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                return oldItem.getName() != null && oldItem.getName().equals(newItem.getName())
                    && (oldItem.getIcon() != null ? oldItem.getIcon().equals(newItem.getIcon()) : newItem.getIcon() == null);
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

            // Load icon with Glide (lazy loading with placeholder and error fallback)
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
                if (clickListener != null) clickListener.onCategoryClick(category);
            });
        }
    }
}
