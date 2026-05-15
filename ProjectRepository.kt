package com.example.gramasuvidha.repository

import android.content.Context
import com.example.gramasuvidha.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray

class ProjectRepository(private val context: Context) {
    fun getProjects(): Flow<List<Project>> = flow {
        try {
            val jsonString = context.assets.open("projects.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val projects = mutableListOf<Project>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                projects.add(
                    Project(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        titleKn = if (obj.has("titleKn")) obj.getString("titleKn") else obj.getString("title"),
                        description = obj.getString("description"),
                        descriptionKn = if (obj.has("descriptionKn")) obj.getString("descriptionKn") else obj.getString("description"),
                        location = obj.getString("location"),
                        locationKn = if (obj.has("locationKn")) obj.getString("locationKn") else obj.getString("location"),
                        budget = obj.getString("budget"),
                        status = obj.getString("status"),
                        progress = obj.getDouble("progress").toFloat(),
                        completionDate = obj.getString("completionDate"),
                        imageUrl = if (obj.has("imageUrl")) obj.getString("imageUrl") else null,
                        beforeImageUrl = if (obj.has("beforeImageUrl")) obj.getString("beforeImageUrl") else null,
                        afterImageUrl = if (obj.has("afterImageUrl")) obj.getString("afterImageUrl") else null,
                        rating = if (obj.has("rating")) obj.getInt("rating") else 0,
                    ),
                )
            }
            emit(projects)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }
}
