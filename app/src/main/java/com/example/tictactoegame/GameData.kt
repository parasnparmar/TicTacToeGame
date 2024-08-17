package com.example.tictactoegame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object GameData {
    private var _gameModel: MutableLiveData<GameModel> = MutableLiveData()
    val gameModel: LiveData<GameModel> = _gameModel
    var myId = ""

    fun saveGameModel(model: GameModel) {
        _gameModel.postValue(model)

        if (model.gameId != "-1") {
            Firebase.firestore.collection("games")
                .document(model.gameId)
                .set(model)
                .addOnSuccessListener {
                    // Optionally handle success
                }
                .addOnFailureListener {
                    // Handle failure
                }
        }
    }

    fun fetchGameModel() {
        gameModel.value?.apply {
            if (gameId != "-1") {
                Firebase.firestore.collection("games")
                    .document(gameId)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            // Handle the error
                            return@addSnapshotListener
                        }

                        val model = value?.toObject(GameModel::class.java)
                        _gameModel.postValue(model)
                    }
            }
        }
    }
}
