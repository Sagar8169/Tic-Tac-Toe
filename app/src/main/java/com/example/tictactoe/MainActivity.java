package com.example.tictactoe;




import static android.content.ContentValues.TAG;




import androidx.appcompat.app.AppCompatActivity;




import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;




import com.example.tictactoe.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;




import java.util.ArrayList;
import java.util.List;




public class MainActivity extends AppCompatActivity {




    private ActivityMainBinding binding;
    private final List<int[]> combinationList = new ArrayList<>();
    private int[] boxPositions = new int[9]; // Default values are 0
    private int playerTurn = 1;
    private int totalSelectedBoxes = 1;
    private int playCount = 0; // Tracks the total number of games played
    private int playerOneScore = 0; // Player X score
    private int playerTwoScore = 0; // Player O score




    private AdView mAdView; // Banner AdView
    private InterstitialAd mInterstitialAd; // Interstitial Ad




    private TextView playCounter; // TextView to display the number of plays
    private Button restartButton; // Button for restarting the match
    private TextView player1ScoreTextView; // Player X score TextView
    private TextView player2ScoreTextView; // Player O score TextView




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {});




        // Setup Banner Ads
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);




        AdView adView2 = findViewById(R.id.adView2);
        adView2.loadAd(adRequest);




        // Load Interstitial Ad
        loadInterstitialAd();




        // Initialize UI components
        playCounter = findViewById(R.id.playCounter);
        restartButton = findViewById(R.id.click_btn);
        player1ScoreTextView = findViewById(R.id.player1score);
        player2ScoreTextView = findViewById(R.id.player2score);




        // Set OnClickListener for restart button
        restartButton.setOnClickListener(v -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
            } else {
                Log.d(TAG, "Interstitial ad not loaded yet.");
            }
            restartMatch();
        });




        // Initialize the game
        initializeGame();
    }




    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-2461177337350544/9508774683", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d(TAG, "Interstitial ad loaded.");
                    }




                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        Log.d(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage());
                    }
                });
    }




    private void initializeGame() {
        combinationList.add(new int[]{0, 1, 2});
        combinationList.add(new int[]{3, 4, 5});
        combinationList.add(new int[]{6, 7, 8});
        combinationList.add(new int[]{0, 3, 6});
        combinationList.add(new int[]{1, 4, 7});
        combinationList.add(new int[]{2, 5, 8});
        combinationList.add(new int[]{2, 4, 6});
        combinationList.add(new int[]{0, 4, 8});




        String getPlayerOneName = getIntent().getStringExtra("playerOne");
        String getPlayerTwoName = getIntent().getStringExtra("playerTwo");




        binding.playerOneName.setText(getPlayerOneName);
        binding.playerTwoName.setText(getPlayerTwoName);




        setupGameBoard();
    }




    private void setupGameBoard() {
        binding.image1.setOnClickListener(view -> performAction((ImageView) view, 0));
        binding.image2.setOnClickListener(view -> performAction((ImageView) view, 1));
        binding.image3.setOnClickListener(view -> performAction((ImageView) view, 2));
        binding.image4.setOnClickListener(view -> performAction((ImageView) view, 3));
        binding.image5.setOnClickListener(view -> performAction((ImageView) view, 4));
        binding.image6.setOnClickListener(view -> performAction((ImageView) view, 5));
        binding.image7.setOnClickListener(view -> performAction((ImageView) view, 6));
        binding.image8.setOnClickListener(view -> performAction((ImageView) view, 7));
        binding.image9.setOnClickListener(view -> performAction((ImageView) view, 8));
    }




    private void performAction(ImageView imageView, int selectedBoxPosition) {
        if (!isBoxSelectable(selectedBoxPosition)) return;




        boxPositions[selectedBoxPosition] = playerTurn;




        if (playerTurn == 1) {
            imageView.setImageResource(R.drawable.ximage);
        } else {
            imageView.setImageResource(R.drawable.oimage);
        }




        handleGameResult();
    }




    private void handleGameResult() {
        if (checkResults()) {
            String winner = (playerTurn == 1) ? binding.playerOneName.getText().toString() : binding.playerTwoName.getText().toString();
            if (playerTurn == 1) {
                playerOneScore++;
            } else {
                playerTwoScore++;
            }
            updateScores();
            showResultDialog(winner + " is the Winner!");
        } else if (totalSelectedBoxes == 9) {
            showResultDialog("Match Draw");
        } else {
            totalSelectedBoxes++;
            changePlayerTurn(playerTurn == 1 ? 2 : 1);
        }
    }




    private void updateScores() {
        player1ScoreTextView.setText("Player X Score: " + playerOneScore);
        player2ScoreTextView.setText("Player O Score: " + playerTwoScore);
    }




    private void showResultDialog(String message) {
        ResultDialog resultDialog = new ResultDialog(MainActivity.this, message, this);
        resultDialog.setCancelable(false);
        resultDialog.show();
        showInterstitialAd();




        // Update play counter after game ends
        playCount++;
        updatePlayCounter();
    }




    private void updatePlayCounter() {
        playCounter.setText("Plays: " + playCount);
    }




    private void changePlayerTurn(int currentPlayerTurn) {
        playerTurn = currentPlayerTurn;
        if (playerTurn == 1) {
            binding.playerOneLayout.setBackgroundResource(R.drawable.black_border);
            binding.playerTwoLayout.setBackgroundResource(R.drawable.white_box);
        } else {
            binding.playerTwoLayout.setBackgroundResource(R.drawable.black_border);
            binding.playerOneLayout.setBackgroundResource(R.drawable.white_box);
        }
    }




    private boolean checkResults() {
        for (int[] combination : combinationList) {
            if (boxPositions[combination[0]] == playerTurn &&
                    boxPositions[combination[1]] == playerTurn &&
                    boxPositions[combination[2]] == playerTurn) {
                return true;
            }
        }
        return false;
    }




    private boolean isBoxSelectable(int boxPosition) {
        return boxPositions[boxPosition] == 0;
    }




    public void restartMatch() {
        boxPositions = new int[9]; // Reset board positions
        playerTurn = 1;
        totalSelectedBoxes = 1;




        binding.image1.setImageResource(R.drawable.white_box);
        binding.image2.setImageResource(R.drawable.white_box);
        binding.image3.setImageResource(R.drawable.white_box);
        binding.image4.setImageResource(R.drawable.white_box);
        binding.image5.setImageResource(R.drawable.white_box);
        binding.image6.setImageResource(R.drawable.white_box);
        binding.image7.setImageResource(R.drawable.white_box);
        binding.image8.setImageResource(R.drawable.white_box);
        binding.image9.setImageResource(R.drawable.white_box);




        loadInterstitialAd();
    }




    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        }
    }
}
