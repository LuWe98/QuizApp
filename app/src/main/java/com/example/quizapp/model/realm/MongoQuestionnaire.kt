package com.example.quizapp.model.realm

import androidx.recyclerview.widget.DiffUtil
import org.bson.types.ObjectId

data class MongoQuestionnaire(
    var id : String = ObjectId().toString(),
    var title : String = "",
    var authorUserName : String = "",
    var questions : List<MongoQuestion> = emptyList()
){

    companion object {
        val DIFF_CALLBACK = object  : DiffUtil.ItemCallback<MongoQuestionnaire>(){
            override fun areItemsTheSame(oldItem: MongoQuestionnaire, newItem: MongoQuestionnaire) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MongoQuestionnaire, newItem: MongoQuestionnaire) = oldItem == newItem
        }
    }
}



/*
open class MongoQuestionnaire(
    @PrimaryKey var id : String = ObjectId().toString(),
    var title : String = "",
    var authorUserName : String = "",
    var questions : RealmList<MongoQuestion> = RealmList()
) : RealmObject(){

    companion object {
        val DIFF_CALLBACK = object  : DiffUtil.ItemCallback<MongoQuestionnaire>(){
            override fun areItemsTheSame(oldItem: MongoQuestionnaire, newItem: MongoQuestionnaire) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MongoQuestionnaire, newItem: MongoQuestionnaire) = oldItem == newItem
        }
    }
}
 */