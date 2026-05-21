package com.dermacare.clinic.auth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dermacare.clinic.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.Holder> {
    private final int[] titles = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };
    private final int[] descriptions = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };
    private final int[] illustrations = {
            R.drawable.ill_onboarding_1,
            R.drawable.ill_onboarding_2,
            R.drawable.ill_onboarding_3
    };

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.tvTitle.setText(titles[position]);
        holder.tvDescription.setText(descriptions[position]);
        holder.imgIllustration.setImageResource(illustrations[position]);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView imgIllustration;
        final TextView tvTitle;
        final TextView tvDescription;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgIllustration = itemView.findViewById(R.id.imgIllustration);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
