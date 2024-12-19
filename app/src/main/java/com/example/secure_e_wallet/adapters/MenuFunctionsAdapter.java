package com.example.secure_e_wallet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secure_e_wallet.R;
import com.example.secure_e_wallet.interfaces.OnMenuItemClickListener;
import com.example.secure_e_wallet.model.MenuItem;

import java.util.List;

public class MenuFunctionsAdapter extends RecyclerView.Adapter<MenuFunctionsAdapter.MenuViewHolder> {
    private Context context;
    private List<MenuItem> menuItems;
    private OnMenuItemClickListener listener;

    public MenuFunctionsAdapter(Context context, List<MenuItem> menuItems, OnMenuItemClickListener listener) {
        this.context = context;
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grid_layout_functions, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        holder.itemIcon.setImageDrawable(menuItem.getIcon());
        holder.itemText.setText(menuItem.getTitle());
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView itemIcon;
        TextView itemText;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            itemText = itemView.findViewById(R.id.itemText);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onMenuItemClick(menuItems.get(getAdapterPosition()));
            }
        }
    }

}
