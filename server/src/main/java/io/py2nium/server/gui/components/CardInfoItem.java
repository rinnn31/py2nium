/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
* Apache 2.0 License
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package io.py2nium.server.gui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import io.py2nium.server.R;

public class CardInfoItem extends ConstraintLayout {

    private ImageView icon;
    private TextView mainText;
    private TextView subText;
    private MaterialCardView mainView;

    public CardInfoItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.card_info_item_layout, this, true);

        icon = findViewById(R.id.ciiIcon);
        mainText = findViewById(R.id.ciiMainText);
        subText = findViewById(R.id.ciiSubText);
        mainView = findViewById(R.id.ciiMainView);

        setupTextWatchers();

        if (attrs != null) {
            applyAttributes(attrs);
        }
    }

    private void setupTextWatchers() {
        subText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                subText.setVisibility(s != null && s.length() > 0 ? VISIBLE : GONE);
            }
        });
    }

    private void applyAttributes(@NonNull AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CardInfoItem);
        try {
            // Text Attributes
            mainText.setText(ta.getString(R.styleable.CardInfoItem_mainText));
            mainText.setTextColor(ta.getColor(R.styleable.CardInfoItem_mainTextColor,
                    ContextCompat.getColor(getContext(), R.color.black)));
            mainText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    ta.getDimension(R.styleable.CardInfoItem_mainTextSize, 18f));

            subText.setText(ta.getString(R.styleable.CardInfoItem_subText));
            subText.setTextColor(ta.getColor(R.styleable.CardInfoItem_subTextColor,
                    ContextCompat.getColor(getContext(), R.color.gray)));

            // Icon Dimensions & Container logic
            setupIconDimensions(ta);

            // Background & Interaction
            int backgroundColor = ta.getColor(R.styleable.CardInfoItem_backgroundColor,
                    ContextCompat.getColor(getContext(), R.color.white));
            mainView.setCardBackgroundColor(backgroundColor);

            float radius = ta.getDimension(R.styleable.CardInfoItem_backgroundRadius,
                    dpToPx(10));
            mainView.setRadius(radius);

            int rippleColor = ta.getColor(R.styleable.CardInfoItem_onClick_backgroundColor, 0);
            if (rippleColor != 0) {
                mainView.setRippleColor(ColorStateList.valueOf(rippleColor));
            }

            mainView.setOnClickListener(v -> performClick());
            updateClickableState();

        } finally {
            ta.recycle();
        }
    }

    private void setupIconDimensions(TypedArray ta) {
        int iconLayoutSize = ta.getDimensionPixelSize(R.styleable.CardInfoItem_iconAreaWidth, dpToPx(60));
        int iconContainerSize = ta.getDimensionPixelSize(R.styleable.CardInfoItem_iconBackgroundSize, (int)(iconLayoutSize * 0.8));
        int iconSize = ta.getDimensionPixelSize(R.styleable.CardInfoItem_iconSize, (int)(iconContainerSize * 0.8));

        // Update the outer container width
        View iconLayout = (View) icon.getParent().getParent();
        updateViewSize(iconLayout, iconLayoutSize, -1);

        // Update the circular CardView
        CardView iconContainer = (CardView) icon.getParent();
        updateViewSize(iconContainer, iconContainerSize, iconContainerSize);
        iconContainer.setRadius(iconContainerSize / 2.0f);
        iconContainer.setCardBackgroundColor(ta.getColor(R.styleable.CardInfoItem_iconBackgroundColor,
                ContextCompat.getColor(getContext(), android.R.color.transparent)));

        // Update the ImageView
        updateViewSize(icon, iconSize, iconSize);
        icon.setImageDrawable(ta.getDrawable(R.styleable.CardInfoItem_icon));
    }

    private void updateViewSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (width != -1) params.width = width;
        if (height != -1) params.height = height;
        view.setLayoutParams(params);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void updateClickableState() {
        if (mainView != null) {
            mainView.setClickable(isClickable());
            mainView.setFocusable(isFocusable());
        }
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        updateClickableState();
    }

    // Helper class to reduce boilerplate in TextWatcher
    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    public ImageView getIconView() { return icon; }
    public TextView getMainTextView() { return mainText; }
    public TextView getSubTextView() { return subText; }
}