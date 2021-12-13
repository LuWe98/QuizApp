package com.example.quizapp.model.databases.mongodb.documents

import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.Degree
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoCourseOfStudies(
    @BsonId val id: String = ObjectId().toHexString(),
    val facultyIds: List<String> = emptyList(),
    val abbreviation: String,
    val name: String,
    val degree: Degree,
    val lastModifiedTimestamp : Long = getTimeMillis()
) {

    val asRoomCourseOfStudies get() = DataMapper.mapMongoCourseOfStudiesToRoomCourseOfStudies(this)

}