package edu.orangecoastcollege.cs273.dnovasky.flagquiz;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by dnova_000 on 10/2/2016.
 * This is a DialogFragment used to display the results at the end of the quiz
 */

public class ResultsDialogFragment extends DialogFragment {
    // Create a new AlertDialog and return it.
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        int totalGuesses = args.getInt("totalGuesses");
        builder.setMessage(getString(R.string.results, totalGuesses,
                (1000 / (double) totalGuesses)));

        // "Reset Quiz" button
        builder.setPositiveButton(R.string.reset_quiz,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,
                                        int i) {
                    }
                });
        return builder.create(); // Return the AlertDialog.
    }
}