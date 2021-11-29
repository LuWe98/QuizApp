package com.example.quizapp.model.databases.mongodb.documents.faculty

import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoFaculty(
    @BsonId var id: String = ObjectId().toHexString(),
    var abbreviation: String,
    var name: String,
    var lastModifiedTimestamp : Long = getTimeMillis()
) {

    val asRoomFaculty : Faculty get() = DataMapper.mapMongoFacultyToRoomFaculty(this)

}