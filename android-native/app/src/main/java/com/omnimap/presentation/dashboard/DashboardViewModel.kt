package com.omnimap.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimap.domain.model.Node
import com.omnimap.domain.repository.OmniMapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.omnimap.data.local.backup.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardViewModel(
    private val repository: OmniMapRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Node>>(emptyList())
    val searchResults: StateFlow<List<Node>> = _searchResults.asStateFlow()

    private val _exportJsonResult = MutableStateFlow<String?>(null)
    val exportJsonResult: StateFlow<String?> = _exportJsonResult.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                if (query.isBlank()) {
                    _searchResults.value = emptyList()
                } else {
                    repository.searchNodes(query).collect { nodes ->
                        _searchResults.value = nodes
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun exportGraph() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val nodes = repository.getAllNodesSync()
                val edges = repository.getAllEdgesSync()
                val json = BackupManager.exportToJson(nodes, edges)
                _exportJsonResult.value = json
            }
        }
    }

    fun clearExportResult() {
        _exportJsonResult.value = null
    }

    fun importGraph(jsonString: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val (nodes, edges) = BackupManager.importFromJson(jsonString)
                    repository.insertNodes(nodes)
                    repository.insertEdges(edges)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}