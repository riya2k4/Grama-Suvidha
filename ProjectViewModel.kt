package com.example.gramasuvidha.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gramasuvidha.model.Project
import com.example.gramasuvidha.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProjectRepository(application)
    
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            repository.getProjects().collect { projectList ->
                _projects.value = projectList
            }
        }
    }

    fun updateProjectRating(projectId: Int, newRating: Int) {
        _projects.value = _projects.value.map {
            if (it.id == projectId) it.copy(rating = newRating) else it
        }
    }
}
