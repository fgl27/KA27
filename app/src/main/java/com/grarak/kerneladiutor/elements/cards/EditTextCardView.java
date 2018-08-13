/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.grarak.kerneladiutor.elements.cards;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DParent;
import com.grarak.kerneladiutor.utils.Utils;

/**
 * Created by willi on 26.12.14.
 */
public class EditTextCardView extends CardViewItem {

    private String value, titleText, base, current, textViewText;

    private OnEditTextCardListener onEditTextCardListener;

    public EditTextCardView(Context context) {
        super(context);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();

                if (value == null) value = "";
                if (base == null) base = "";
                if (titleText == null) titleText = "";
                current = value;

                ViewGroup base_parent = (ViewGroup) findViewById(R.id.base_parent);
                View alertLayout = LayoutInflater.from(context).inflate(R.layout.global_offset_view, base_parent, false);

                final TextView textView = (TextView) alertLayout.findViewById(R.id.offset_text);
                textViewText = value + base;
                textView.setText(textViewText);

                AppCompatButton minus = (AppCompatButton) alertLayout.findViewById(R.id.button_minus);
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            value = String.valueOf(Utils.stringToInt(value) - 5);
                            textViewText = value + base;
                            textView.setText(textViewText);
                        } catch (NumberFormatException e) {
                            textView.setText(textViewText);
                        }
                    }
                });

                AppCompatButton plus = (AppCompatButton) alertLayout.findViewById(R.id.button_plus);
                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            value = String.valueOf(Utils.stringToInt(value) + 5);
                            textViewText = value + base;
                            textView.setText(textViewText);
                        } catch (NumberFormatException e) {
                            textView.setText(textViewText);
                        }
                    }
                });

                if (Utils.DARKTHEME) {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.textcolor_dark));
                    minus.setTextColor(ContextCompat.getColor(context, R.color.textcolor_dark));
                    plus.setTextColor(ContextCompat.getColor(context, R.color.textcolor_dark));
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(context,
                        (Utils.DARKTHEME ? R.style.AlertDialogStyleDark : R.style.AlertDialogStyleLight))
                    .setTitle(titleText)
                    .setMessage(String.format(context.getString(R.string.current_value), value + base))
                    .setView(alertLayout)
                    .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            value = current;
                        }
                    })
                    .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (onEditTextCardListener != null && !current.equals(value))
                                onEditTextCardListener.onApply(EditTextCardView.this, value);
                        }
                    });

                AlertDialog dialog = alert.create();
                dialog.show();
            }
        });
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void titleText(String title) {
        this.titleText = title;
    }

    public void setOnEditTextCardListener(OnEditTextCardListener onEditTextCardListener) {
        this.onEditTextCardListener = onEditTextCardListener;
    }

    public interface OnEditTextCardListener {
        void onApply(EditTextCardView editTextCardView, String value);
    }

    public static class DEditTextCard extends DParent {

        private EditTextCardView editTextCardView;

        private String title, description, value, base;

        private OnDEditTextCardListener onDEditTextCardListener;

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
            super.onBindViewHolder(viewHolder);

            editTextCardView = (EditTextCardView) viewHolder.itemView;

            if (title != null) {
                editTextCardView.setTitle(title);
                editTextCardView.titleText(title);
            };
            if (description != null) editTextCardView.setDescription(description);
            if (value != null) editTextCardView.setValue(value);
            if (base != null) editTextCardView.setBase(base);

            editTextCardView.setOnEditTextCardListener(new EditTextCardView.OnEditTextCardListener() {
                @Override
                public void onApply(EditTextCardView editTextCardView, String value) {
                    if (onDEditTextCardListener != null)
                        onDEditTextCardListener.onApply(DEditTextCard.this, value);
                }
            });
        }

        @Override
        public View getView(ViewGroup viewGroup) {
            return new EditTextCardView(viewGroup.getContext());
        }

        public void setTitle(String title) {
            this.title = title;
            if (editTextCardView != null) editTextCardView.setTitle(title);
        }

        public void setBase(String base) {
            this.base = base;
            if (editTextCardView != null) editTextCardView.setBase(base);
        }

        public void setDescription(String description) {
            this.description = description;
            if (editTextCardView != null) editTextCardView.setDescription(description);
        }

        public void setValue(String value) {
            this.value = value;
            if (editTextCardView != null) editTextCardView.setValue(value);
        }

        public void setOnDEditTextCardListener(OnDEditTextCardListener onDEditTextCardListener) {
            this.onDEditTextCardListener = onDEditTextCardListener;
        }

        public interface OnDEditTextCardListener {
            void onApply(DEditTextCard dEditTextCard, String value);
        }

    }

}
