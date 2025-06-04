/*
 * Copyright (C) 2025 LumineDroid
 * SPDX-License-Identifier: Apache-2.0
 */

package org.luminedroid.extensions.category.about;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class TeamMemberPreference extends Preference {

    private String imageUrl;

    public TeamMemberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.luminedroid_team_card);
    }

    public void setImageUrl(String url) {
        this.imageUrl = url;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView imageView = (ImageView) holder.findViewById(R.id.team_avatar);
        if (imageView != null && imageUrl != null) {
            Glide.with(getContext())
                 .load(imageUrl)
                 .apply(RequestOptions.circleCropTransform())
                 .into(imageView);
        }
    }
}
