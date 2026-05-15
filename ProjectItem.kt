package com.example.gramasuvidha.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gramasuvidha.R
import com.example.gramasuvidha.model.Project

@Composable
fun ProjectItem(
    project: Project,
    onReportIssue: (Project) -> Unit,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val isKn = currentLocale.contains("kn")
    val displayTitle = if (isKn) project.titleKn else project.title
    val displayDesc = if (isKn) project.descriptionKn else project.description
    val displayLocation = if (isKn) project.locationKn else project.location

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column {
            // Project Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = project.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(android.R.drawable.ic_dialog_map) // Shows a map icon if no internet
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = displayTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Badge(containerColor = if (project.status == "Completed") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary) {
                        Text(text = project.status, color = Color.White, modifier = Modifier.padding(4.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = displayDesc, style = MaterialTheme.typography.bodyMedium, maxLines = 2)

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = displayLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${stringResource(R.string.budget)} ${project.budget}", fontWeight = FontWeight.Bold)
                    Text(text = project.completionDate, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "${stringResource(R.string.progress)} ${(project.progress * 100).toInt()}%")
                LinearProgressIndicator(
                    progress = { project.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 4.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.selectableGroup()) {
                        (1..5).forEach { i ->
                            IconButton(onClick = { onRatingChange(i) }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = if (i <= project.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (i <= project.rating) Color(0xFFFFB300) else Color.Gray,
                                )
                            }
                        }
                    }
                    Button(onClick = { onReportIssue(project) }) {
                        Text(stringResource(R.string.report_issue))
                    }
                }
            }
        }
    }
}
