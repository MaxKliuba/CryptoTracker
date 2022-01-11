package com.maxclub.android.cryptotracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

public class EditTimeIntervalDialogFragment extends DialogFragment {

    private static final String ARG_TIME_INTERVAL = "time_interval";

    public static final String EXTRA_TIME_INTERVAL = "com.maxclub.android.cryptotracker.EditTimeIntervalDialogFragment.timeInterval";

    String inputText;

    public static EditTimeIntervalDialogFragment newInstance(int timeInterval) {
        Bundle args = new Bundle();
        args.putInt(ARG_TIME_INTERVAL, timeInterval);

        EditTimeIntervalDialogFragment fragment = new EditTimeIntervalDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        int timeInterval = (int) getArguments().getInt(ARG_TIME_INTERVAL);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_time_interval, null);

        inputText = timeInterval + "";
        TextInputEditText inputEditText = (TextInputEditText) view.findViewById(R.id.time_interval_edit_text);
        inputEditText.append(inputText);
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {

                    if (charSequence.length() != 0) {
                        int number = Integer.parseInt(charSequence.toString());

                        if (number < 0 || number > 3600) {
                            throw new NumberFormatException();
                        }
                    }
                    inputText = charSequence.toString();
                } catch (NumberFormatException numberFormatException) {
                    numberFormatException.printStackTrace();
                    Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();

                    inputEditText.setText(inputText);
                    inputEditText.setSelection(inputText.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.avg_time_interval_title)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_CANCELED, timeInterval);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            int number = Integer.parseInt(inputEditText.getText().toString());

                            if (number < 0 || number > 3600) {
                                throw new NumberFormatException();
                            }

                            sendResult(Activity.RESULT_OK, number);
                        } catch (NumberFormatException numberFormatException) {
                            numberFormatException.printStackTrace();
                            Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_SHORT).show();
                            sendResult(Activity.RESULT_CANCELED, timeInterval);
                        }

                    }
                })
                .setCancelable(true);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button negativeButton = (Button) dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positiveButton = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);

                negativeButton.setTextColor(Color.GRAY);
                positiveButton.setTextColor(getResources().getColor(R.color.color_primary));

                Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
                positiveButton.setTypeface(typeface);

                negativeButton.invalidate();
                positiveButton.invalidate();
            }
        });

        return dialog;
    }

    private void sendResult(int resultCode, int timeInterval) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME_INTERVAL, timeInterval);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
