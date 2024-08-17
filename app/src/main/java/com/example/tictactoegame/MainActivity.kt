package com.example.tictactoegame

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoegame.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlayOffline.setOnClickListener {
            createOfflineGame()
        }

        binding.btnCreateGameOnline.setOnClickListener {
            createOnlineGame()
        }

        binding.btnJoinGameOnline.setOnClickListener {
            joinOnlineGame()
        }
    }

    private fun createOfflineGame() {
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.JOINED
            )
        )
        startGame()
    }

    private fun createOnlineGame() {
        GameData.myId = "X"
        val gameId = Random.nextInt(1000..9999).toString()
        Firebase.firestore.collection("games")
            .document(gameId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    createOnlineGame() // Retry with a new game ID
                } else {
                    val gameModel = GameModel(
                        gameStatus = GameStatus.CREATED,
                        gameId = gameId,
                        currentPlayer = "X"
                    )
                    GameData.saveGameModel(gameModel)
                    Firebase.firestore.collection("games")
                        .document(gameId)
                        .set(gameModel)
                        .addOnSuccessListener {
                            startGame()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to create game. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun joinOnlineGame() {
        val gameId = binding.etGameId.text.toString()
        if (gameId.isEmpty()) {
            binding.etGameId.error = "Please enter game ID"
            return
        }

        GameData.myId = "O"

        val gameDocument = Firebase.firestore.collection("games").document(gameId)

        gameDocument.get().addOnSuccessListener { document ->
            val model = document?.toObject(GameModel::class.java)
            if (model == null) {
                binding.etGameId.error = "Invalid game ID"
            } else {
                // Update the game status to JOINED
                model.gameStatus = GameStatus.JOINED
                Firebase.firestore.collection("games").document(gameId)
                    .set(model)
                    .addOnSuccessListener {
                        GameData.saveGameModel(model)
                        startGame()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to join game. Please try again.", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error joining game. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGame() {
        startActivity(Intent(this, GameActivity::class.java))
    }
}
