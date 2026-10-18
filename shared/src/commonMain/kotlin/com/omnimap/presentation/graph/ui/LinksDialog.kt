package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.omnimap.domain.model.Edge
import com.omnimap.domain.model.EdgeType
import com.omnimap.domain.model.Node
import com.omnimap.presentation.theme.NodeTypeGlyph

val EdgeType.label: String
    get() = when (this) {
        EdgeType.PARENT_OF -> "Parent of"
        EdgeType.DEPENDS_ON -> "Depends on"
        EdgeType.RELATED_TO -> "Related to"
    }

@Composable
private fun EdgeTypeChips(selected: EdgeType?, onSelect: (EdgeType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        EdgeType.entries.forEach { type ->
            FilterChip(selected = type == selected, onClick = { onSelect(type) }, label = { Text(type.label) })
        }
    }
}

@Composable
private fun DialogCard(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(0.9f).padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(24.dp), content = content)
        }
    }
}

/** Shown after dragging a link handle onto a node: pick how the two relate. */
@Composable
fun LinkTypeDialog(source: Node, target: Node, onDismiss: () -> Unit, onConfirm: (EdgeType) -> Unit) {
    DialogCard(onDismiss) {
        Text("Link nodes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "“${source.title}” → “${target.title}”",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EdgeType.entries.forEach { type ->
                OutlinedButton(onClick = { onConfirm(type) }, modifier = Modifier.fillMaxWidth()) {
                    Text("${source.title.take(24)} · ${type.label.lowercase()} · ${target.title.take(24)}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel") }
    }
}

/**
 * Manage one node's links: change a link's type, remove it, or link to another node
 * picked from a searchable list.
 */
@Composable
fun LinksDialog(
    node: Node,
    nodes: Map<String, Node>,
    edges: List<Edge>,
    onDismiss: () -> Unit,
    onCreate: (targetId: String, type: EdgeType) -> Unit,
    onChangeType: (edgeId: String, type: EdgeType) -> Unit,
    onDelete: (edgeId: String) -> Unit
) {
    val links = edges.filter { it.sourceId == node.id || it.targetId == node.id }
    var query by remember { mutableStateOf("") }
    var target by remember { mutableStateOf<Node?>(null) }
    var newType by remember { mutableStateOf(EdgeType.RELATED_TO) }
    val linkedIds = links.map { if (it.sourceId == node.id) it.targetId else it.sourceId }.toSet()
    val candidates = nodes.values
        .filter { it.id != node.id && it.id !in linkedIds && it.title.contains(query, ignoreCase = true) }
        .sortedBy { it.title.lowercase() }

    DialogCard(onDismiss) {
        Text("Links for “${node.title}”", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2)
        Spacer(Modifier.height(16.dp))

        if (links.isEmpty()) {
            Text("No links yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        } else {
            links.forEach { edge ->
                val outgoing = edge.sourceId == node.id
                val other = nodes[if (outgoing) edge.targetId else edge.sourceId] ?: return@forEach
                var menuOpen by remember(edge.id) { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    NodeTypeGlyph(other.type)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(other.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            if (outgoing) "This node · ${edge.type.label.lowercase()} · it" else "It · ${edge.type.label.lowercase()} · this node",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Box {
                        TextButton(onClick = { menuOpen = true }) { Text(edge.type.label) }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            EdgeType.entries.forEach { type ->
                                DropdownMenuItem(text = { Text(type.label) }, onClick = { onChangeType(edge.id, type); menuOpen = false })
                            }
                        }
                    }
                    IconButton(onClick = { onDelete(edge.id) }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove link to ${other.title}", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
        Text("Add a link", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; target = null },
            placeholder = { Text("Search nodes…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LazyColumn(modifier = Modifier.heightIn(max = 200.dp).padding(top = 4.dp)) {
            items(candidates, key = { it.id }) { candidate ->
                val isPicked = candidate.id == target?.id
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { target = candidate }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    RadioButton(selected = isPicked, onClick = { target = candidate })
                    NodeTypeGlyph(candidate.type)
                    Spacer(Modifier.width(8.dp))
                    Text(candidate.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        EdgeTypeChips(newType) { newType = it }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onDismiss) { Text("Done") }
            Button(
                onClick = { target?.let { onCreate(it.id, newType); target = null; query = "" } },
                enabled = target != null
            ) { Text("Add link") }
        }
    }
}
