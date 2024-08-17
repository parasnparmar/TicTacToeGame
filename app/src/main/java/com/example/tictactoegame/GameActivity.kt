package com.example.tictactoegame

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoegame.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityGameBinding
    private var gameModel: GameModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GameData.fetchGameModel()

        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

        binding.btnStartGame.setOnClickListener {
            if (gameModel?.gameStatus == GameStatus.FINISHED) {
                resetGameBoard() // Restart the game
            } else {
                startGame() // Start a new game if not already started
            }
        }

        GameData.gameModel.observe(this) {
            gameModel = it
            setUI()
        }
    }


    fun setUI() {
        gameModel?.apply {
            binding.btn0.text = filledPos[0]
            binding.btn1.text = filledPos[1]
            binding.btn2.text = filledPos[2]
            binding.btn3.text = filledPos[3]
            binding.btn4.text = filledPos[4]
            binding.btn5.text = filledPos[5]
            binding.btn6.text = filledPos[6]
            binding.btn7.text = filledPos[7]
            binding.btn8.text = filledPos[8]

            when (gameStatus) {
                GameStatus.CREATED -> {
                    binding.btnStartGame.visibility = View.INVISIBLE
                    binding.txtGameStatus.text = "Game ID :" + gameId
                }
                GameStatus.JOINED -> {
                    binding.btnStartGame.visibility = View.VISIBLE
                    binding.txtGameStatus.text = "Click on start game"
                }
                GameStatus.INPROGRESS -> {
                    binding.btnStartGame.visibility = View.INVISIBLE
                    binding.txtGameStatus.text =
                        if (GameData.myId == currentPlayer) "Your turn" else "$currentPlayer's turn"
                }
                GameStatus.FINISHED -> {
                    binding.btnStartGame.visibility = View.VISIBLE
                    binding.btnStartGame.text = "Restart Game"
                    binding.txtGameStatus.text =
                        if (winner.isNotEmpty()) {
                            if (GameData.myId == winner) "You Won!" else "$winner Won!"
                        } else "Draw!"
                }
            }
        }
    }

    fun startGame() {
        gameModel?.apply {
            if (gameStatus == GameStatus.CREATED || gameStatus == GameStatus.JOINED) {
                updateGameData(
                    GameModel(
                        gameId = gameId,
                        gameStatus = GameStatus.INPROGRESS,
                        currentPlayer = "X" // Reset to starting player if needed
                    )
                )
            }
        }
    }


    fun updateGameData(model: GameModel) {
        GameData.saveGameModel(model)
    }

    fun checkForWinner() {
        val winningPos = arrayOf(
            intArrayOf(0, 1, 2),
            intArrayOf(3, 4, 5),
            intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6),
            intArrayOf(1, 4, 7),
            intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8),
            intArrayOf(2, 4, 6),
        )

        gameModel?.apply {
            var hasWinner = false

            for (i in winningPos) {
                if (
                    filledPos[i[0]] == filledPos[i[1]] &&
                    filledPos[i[1]] == filledPos[i[2]] &&
                    filledPos[i[0]].isNotEmpty()
                ) {
                    gameStatus = GameStatus.FINISHED
                    winner = filledPos[i[0]]
                    hasWinner = true

                    // Load and apply the pulse animation
                    val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.win_animation)
                    i.forEach { pos ->
                        when (pos) {
                            0 -> binding.btn0.startAnimation(animation)
                            1 -> binding.btn1.startAnimation(animation)
                            2 -> binding.btn2.startAnimation(animation)
                            3 -> binding.btn3.startAnimation(animation)
                            4 -> binding.btn4.startAnimation(animation)
                            5 -> binding.btn5.startAnimation(animation)
                            6 -> binding.btn6.startAnimation(animation)
                            7 -> binding.btn7.startAnimation(animation)
                            8 -> binding.btn8.startAnimation(animation)
                        }
                    }
                    break
                }
            }

            if (!hasWinner && filledPos.none { it.isEmpty() }) {
                gameStatus = GameStatus.FINISHED
                winner = ""

                // Load and apply the shake animation
                val drawAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.draw_animation)
                binding.btn0.startAnimation(drawAnimation)
                binding.btn1.startAnimation(drawAnimation)
                binding.btn2.startAnimation(drawAnimation)
                binding.btn3.startAnimation(drawAnimation)
                binding.btn4.startAnimation(drawAnimation)
                binding.btn5.startAnimation(drawAnimation)
                binding.btn6.startAnimation(drawAnimation)
                binding.btn7.startAnimation(drawAnimation)
                binding.btn8.startAnimation(drawAnimation)
            }

            updateGameData(this)
        }
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Game not started", Toast.LENGTH_SHORT).show()
                return
            }

            // game is in progress
            if (gameId != "-1" && currentPlayer != GameData.myId) {
                Toast.makeText(applicationContext, "Not your turn", Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPos = (v?.tag as String).toInt()
            if (filledPos[clickedPos].isEmpty()) {
                filledPos[clickedPos] = currentPlayer
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                checkForWinner()
                updateGameData(this)
            }
        }
    }

    // Function to reset the game board
    fun resetGameBoard() {
        gameModel?.apply {
            filledPos = MutableList(9) { "" } // Reset the board positions
            gameStatus = GameStatus.INPROGRESS // Set the game status to in progress
            winner = "" // Clear the winner
            currentPlayer = "X" // Optionally, reset the current player to "X"
            updateGameData(this)

            // Clear all animations
            val buttons = listOf(binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5, binding.btn6, binding.btn7, binding.btn8)
            buttons.forEach { button ->
                button.clearAnimation()
            }

            // Ensure the game is started immediately after reset
            setUI()
        }
    }

}
