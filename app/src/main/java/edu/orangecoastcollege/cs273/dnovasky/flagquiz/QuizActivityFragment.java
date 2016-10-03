package edu.orangecoastcollege.cs273.dnovasky.flagquiz;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * QuizActivityFragment contains the flag Quiz logic (correct/incorrect/statistics).
 * It also handles the delay of guessing correctly so that users can see the "Correct!"
 * message before the next flag is displayed.
 */
public class QuizActivityFragment extends Fragment {
    // String used when logging error messages
    private static final String TAG = "FlagQuiz Activity";

    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList; // Will contain flag file names.
    private List<String> quizCountriesList; // Will contain countries in the current quiz.
    private Set<String> regionSet; // Will contain world regions in the current quiz.
    private String correctAnswer; // Will contain the correct country for the current flag.
    private int totalGuesses; // Will contain the total number of guesses made.
    private int correctAnswers; // Will contain the number of correct guesses.
    private int guessRows; // Will contain the number of rows displaying guess buttons.
    private SecureRandom random; // Will be used to randomize the quiz.
    private Handler handler; // Will be used to delay the loading of the next flag.

    private TextView questionNumberTextView; // Shows current question number.
    private ImageView flagImageView; // Displays a flag.
    private LinearLayout[] guessLinearLayouts; // Rows of answer buttons
    private TextView answerTextView; // Displays correct answer

    /**
     * Configures the QuizActivityFragment when it's view is created
     * @param inflater the layout inflater.
     * @param container the view group container in which the fragment resides.
     * @param savedInstanceState any saved state to restore in this fragment
     * @return the root view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        // Get references to GUI components
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // Configure listeners for the guess buttons
        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        // Set questionNumberTextView's text.
        questionNumberTextView.setText(
                getString(R.string.question, 1, FLAGS_IN_QUIZ));
        return view; // Returns the fragment'sview for display;
    }

    /**
     * updateGuessRows is called from QuizActivity when the app is launched and each time
     * the user changes the number of guess buttons to display with each flag.
     * @param sharedPreferences the shared preferences from preferences.xml
     */
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // Get the number of guess buttons that should be displayed.
        String choices =
                sharedPreferences.getString(QuizActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // Hide all guess button LinearLayouts
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        // Display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    /**
     * Updates the world regions in the quiz based on values in the shared preferences.
     * @param sharedPreferences the shared preferences from preferences.xml
     */
    public void updateRegions(SharedPreferences sharedPreferences) {
        regionSet = sharedPreferences.getStringSet(QuizActivity.REGIONS, null);
    }

    /**
     * Configure and start up a new Quiz based on the settings.
     */
    public void resetQuiz() {
        // Use AssetManager to get image file names for enabled regions.
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {
            // Loop through each region.
            for (String region : regionSet){
                // Get a list of all flag image files in this region
                String[] paths = assets.list(region);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch (IOException e){
            Log.e(TAG, "Error loading image file name", e);
        }

        correctAnswers = 0; // Resets the number of correct answers made.
        totalGuesses = 0; // Resets the total number of guesses made.
        quizCountriesList.clear(); // Clears the previous list of quiz countries

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        // Adds FLAGS_IN_QUIZ random file names to the quizCountriesList.
        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);

            // Get the random file name.
            String filename = fileNameList.get(randomIndex);

            // If the region is enabled and it hasn't already been chosen
            if (!quizCountriesList.contains(filename)) {
                quizCountriesList.add(filename); // add the file to the list
                ++flagCounter;
            }
        }
        loadNextFlag(); // Starts the quiz by loading the first flag.
    }

    /**
     * After user guesses a flag correctly, load next flag.
     */
    private void loadNextFlag() {
        // Get the file name of the next flag and remove it from the list
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // Update the correct answer.
        answerTextView.setText(""); // Clear the answerTextView.

        // Display current question number.
        questionNumberTextView.setText(getString(
            R.string.question, correctAnswers + 1, FLAGS_IN_QUIZ));
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // Use AssetManager to load next image from assets folder.
        AssetManager assets = getActivity().getAssets();

        // Get an InputStream to the asset representing the next flag and
        // try to open that InputStream.
        try (InputStream stream =
                assets.open(region + "/" + nextImage + ".png")) {
            // Load the asset as a drawable and display it on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
        }
        catch(IOException e) {
            Log.e(TAG, "Error loading " + nextImage, e);
        }

        Collections.shuffle(fileNameList); // Shuffles file names.

        // Put the correct answer at the end of the fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // Add 2, 4, 6, or 8 guess buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++) {
            // Place button in currentTableRow
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                // Get reference to button to configure.
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // Get country name and set it as newGuessButton's text
                String fileName = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(fileName));
            }
        }

        // Randomly replace one button's text with the correct answer
        int row = random.nextInt(guessRows); // Pick random row.
        int column = random.nextInt(2); // Pick random column.
        LinearLayout randomRow = guessLinearLayouts[row]; // Get the row.
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    /**
     *  Parses the country flag file name (e.g. Oceania-American_Samoa.png) and returns
     *  the country name (e.g. American Samoa), replacing underscores with spaces.
     * @param fileName the flag file name
     * @return The country name, parsed from the file name
     */
    private String getCountryName(String fileName){
        return (fileName.substring
                (fileName.indexOf('-') + 1, fileName.length()))
                .replaceAll("_", " ");
    }

    /**
     * Utility method that disables all answer buttons
     */
    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int column = 0; column < guessRow.getChildCount(); column++) {
                guessRow.getChildAt(column).setEnabled(true);
            }
        }
    }

    /**
     * Called when a guess button is clicked. this listener is used for all button in the
     * flag quiz.
     */
    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button guessButton = ((Button) view);
            String guess = guessButton.getText().toString() + "!";
            String answer = getCountryName(correctAnswer) + "!";
            ++totalGuesses; // Increment the number of guesses the user has made.

            if(guess.equals(answer)) { // If the guess is correct
                ++correctAnswers; // Increment the number of correct answers.

                // Display correct answer in green text
                answerTextView.setText(answer);
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer,
                                getContext().getTheme()));

                disableButtons(); // Disable all guess buttons.

                // If the user has correctly identified FLAGS_IN_QUIZ flags.
                if (correctAnswers == FLAGS_IN_QUIZ) {
                    // DialogFragment to display quiz stats and start new quiz
                    DialogFragment quizResults = new ResultsDialogFragment();
                    Bundle args = new Bundle();
                    args.putInt("totalGuesses", totalGuesses);
                    quizResults.setArguments(args);
                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(), "dialog");
                    resetQuiz();
                }
                else { // Answer is correct but quiz is not over.
                    // Load the next flag after a 2 second delay.
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadNextFlag();
                        }
                    }, 2000); // 2000 millisecond for 2-second delay.
                }
            }
            else { // Answer was incorrect
                // Display "Incorrect!" in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(
                        R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false);
            }
        }
    };
}
