package com.example.kamu_kamu.models

class ModelComment {

    //variables
    var id = ""
    var recipeId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""


    //empty constructor, requires by firebase
    constructor()

    //param constructor
    constructor(id: String, recipeId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.recipeId = recipeId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }





}