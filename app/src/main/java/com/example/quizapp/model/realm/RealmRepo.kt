package com.example.quizapp.model.realm

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.extensions.launch
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.coroutines.*
import java.util.concurrent.Executors

val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

class RealmRepo(private val application: Application) {

    private val mainRealm: Realm
    private lateinit var backgroundRealm: Realm
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        Realm.init(application)
        val realmName = "My Project"
        val config = RealmConfiguration.Builder().name(realmName).build()
        mainRealm = Realm.getInstance(config)

        applicationScope.launch(context = dispatcher) {
            backgroundRealm = Realm.getInstance(config)
        }
    }

//
//    fun getQuestionnaireResultsWith(id : String) : RealmResults<MongoQuestionnaire>{
//        return  mainRealm.where(MongoQuestionnaire::class.java).equalTo("id", id).findAllAsync()
//    }
//
//    fun insertTest(mongoQuestionnaire: MongoQuestionnaire){
//        applicationScope.launch(context = dispatcher) {
//            backgroundRealm.executeTransaction {
//                it.insertOrUpdate(mongoQuestionnaire)
//            }
//        }
//    }
//
//    fun getQuestionnaireWithId(id : String) : MongoQuestionnaire?{
//        return backgroundRealm.where(MongoQuestionnaire::class.java).equalTo("id", id).findFirst()
//    }
//
//    fun getQuestionWithId(id : String) : MongoQuestion?{
//        return backgroundRealm.where(MongoQuestion::class.java).equalTo("id", id).findFirst()
//    }
//
//    fun deleteQuestionWith(id : String) {
//        backgroundRealm.executeTransaction {
//            it.where(MongoQuestion::class.java).equalTo("id", id).findFirst()?.deleteFromRealm()
//        }
//    }
//
//    fun deleteQuestionnaireWithId(id : String){
//        backgroundRealm.executeTransaction {
//            it.where(MongoQuestionnaire::class.java).equalTo("id", id).findFirst()?.deleteFromRealm()
//        }
//    }
//
//    fun updateQuestion(id : String, ){
//        backgroundRealm.executeTransaction {
//           // it.insertOrUpdate()
//        }
//    }
}

inline fun AppCompatActivity.launchForBackgroundRealm(crossinline block: suspend CoroutineScope.() -> Unit){
    launch(dispatcher = dispatcher) {
        block.invoke(this)
    }
}