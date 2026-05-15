package com.example.gramasuvidha

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import coil.compose.AsyncImage
import com.example.gramasuvidha.model.Project
import com.example.gramasuvidha.ui.ProjectItem
import com.example.gramasuvidha.ui.theme.GramaSuvidhaTheme
import com.example.gramasuvidha.viewmodel.ProjectViewModel
import kotlinx.coroutines.delay

enum class AppScreen {
    Login,
    LanguageSelection,
    MainList
}

class MainActivity : AppCompatActivity() {
    private val viewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GramaSuvidhaTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.Login) }
                val projects by viewModel.projects.collectAsState()
                var selectedProjectForIssue by remember { mutableStateOf<Project?>(null) }
                var selectedProjectForDetail by remember { mutableStateOf<Project?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        AppScreen.Login -> {
                            LoginScreen(onLoginSuccess = { currentScreen = AppScreen.LanguageSelection })
                        }
                        AppScreen.LanguageSelection -> {
                            LanguageSelectionScreen(onLanguageSelected = { langCode ->
                                setLocale(langCode)
                                currentScreen = AppScreen.MainList
                            })
                        }
                        AppScreen.MainList -> {
                            ProjectListScreen(
                                projects = projects,
                                onProjectClick = { selectedProjectForDetail = it },
                                onReportIssue = { selectedProjectForIssue = it },
                                onRatingChange = { project, rating -> viewModel.updateProjectRating(project.id, rating) },
                                onLanguageToggle = { currentScreen = AppScreen.LanguageSelection }
                            )
                        }
                    }

                    // Dialogs
                    if (selectedProjectForDetail != null) {
                        ProjectDetailDialog(
                            project = selectedProjectForDetail!!,
                            onDismiss = { selectedProjectForDetail = null }
                        )
                    }

                    if (selectedProjectForIssue != null) {
                        ReportIssueDialog(
                            projectTitle = selectedProjectForIssue!!.title,
                            onDismiss = { selectedProjectForIssue = null },
                            onSubmit = { issueText ->
                                Toast.makeText(this, "Issue submitted: $issueText", Toast.LENGTH_LONG).show()
                                selectedProjectForIssue = null
                            }
                        )
                    }
                }
            }
        }
    }

    private fun setLocale(langCode: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var emailPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.welcome),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = emailPhone,
            onValueChange = { emailPhone = it },
            label = { Text(stringResource(R.string.email_phone)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLoginSuccess,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(stringResource(R.string.login), fontSize = 18.sp)
        }
    }
}

@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.select_language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onLanguageSelected("en") },
            modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
        ) {
            Text(stringResource(R.string.english), fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onLanguageSelected("kn") },
            modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
        ) {
            Text(stringResource(R.string.kannada), fontSize = 20.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    projects: List<Project>,
    onProjectClick: (Project) -> Unit,
    onReportIssue: (Project) -> Unit,
    onRatingChange: (Project, Int) -> Unit,
    onLanguageToggle: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val isKn = currentLocale.contains("kn")
    val projectsToDisplay = projects.filter { project ->
        val title = if (isKn) project.titleKn else project.title
        val desc = if (isKn) project.descriptionKn else project.description
        val location = if (isKn) project.locationKn else project.location
        
        val matchesSearch = title.contains(searchQuery, ignoreCase = true) || 
                          desc.contains(searchQuery, ignoreCase = true) ||
                          location.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Ongoing" -> project.status != "Completed"
            "Completed" -> project.status == "Completed"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title)) },
                actions = {
                    IconButton(onClick = onLanguageToggle) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = "Language")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("All", "Ongoing", "Completed")) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(projectsToDisplay) { project ->
                    ProjectItem(
                        project = project,
                        onReportIssue = { onReportIssue(project) },
                        onRatingChange = { rating -> onRatingChange(project, rating) },
                        modifier = Modifier.clickable { onProjectClick(project) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportIssueDialog(projectTitle: String, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${stringResource(R.string.report_issue)}: $projectTitle") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.describe_issue)) },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        },
        confirmButton = {
            Button(onClick = { onSubmit(text) }, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ProjectDetailDialog(project: Project, onDismiss: () -> Unit) {
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(project.title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = project.beforeImageUrl,
                        contentDescription = "Before",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                    AsyncImage(
                        model = project.afterImageUrl,
                        contentDescription = "After",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(if (currentLocale.contains("kn")) project.descriptionKn else project.description)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${stringResource(R.string.location)} ${if (currentLocale.contains("kn")) project.locationKn else project.location}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("${stringResource(R.string.budget)} ${project.budget}", fontWeight = FontWeight.Bold)
                Text("${stringResource(R.string.progress)} ${(project.progress * 100).toInt()}%")
                LinearProgressIndicator(progress = { project.progress }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                Text("${stringResource(R.string.target_date)} ${project.completionDate}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                
                AIImageInsight()

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.ai_analysis),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.ai_mock_response,
                            (project.progress * 100).toInt(),
                            project.budget,
                            project.completionDate
                        ),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
fun AIImageInsight() {
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var showAIImage by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { isAnalyzing = true },
                enabled = !isAnalyzing && analysisResult == null,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(4.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.run_ai_visual_scan), style = MaterialTheme.typography.labelSmall)
            }
            
            Button(
                onClick = { showAIImage = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                contentPadding = PaddingValues(4.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.ai_upload_latest), style = MaterialTheme.typography.labelSmall)
            }
        }

        if (isAnalyzing) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.ai_analyzing), style = MaterialTheme.typography.labelSmall)
            
            LaunchedEffect(Unit) {
                delay(2500)
                isAnalyzing = false
                analysisResult = "AI Verified"
            }
        }

        if (showAIImage) {
            Card(modifier = Modifier.padding(top = 12.dp).fillMaxWidth().height(120.dp)) {
                Box {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1590674899484-d5640e854abe?q=80&w=400",
                        contentDescription = "AI Uploaded",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Badge(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                    ) {
                        Text("AI UPLOADED: JUST NOW", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        analysisResult?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.ai_verification_success),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    GramaSuvidhaTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LanguageSelectionScreenPreview() {
    GramaSuvidhaTheme {
        LanguageSelectionScreen(onLanguageSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectListScreenPreview() {
    val mockProjects = listOf(
        Project(
            id = 1,
            title = "Main Road Repair",
            titleKn = "ಮುಖ್ಯ ರಸ್ತೆ ದುರಸ್ತಿ",
            description = "Repairing the village road.",
            descriptionKn = "ಗ್ರಾಮದ ರಸ್ತೆ ದುರಸ್ತಿ.",
            location = "Main Square",
            locationKn = "ಮುಖ್ಯ ಚೌಕ",
            budget = "₹5,00,000",
            status = "In Progress",
            progress = 0.65f,
            completionDate = "25 Dec 2024",
            imageUrl = "https://images.unsplash.com/photo-1598414440263-d1f4640106a7?q=80&w=400",
            beforeImageUrl = "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?q=80&w=400",
            afterImageUrl = "https://images.unsplash.com/photo-1598414440263-d1f4640106a7?q=80&w=400",
            rating = 4
        ),
        Project(
            id = 2,
            title = "Borewell Install",
            titleKn = "ಕೊಳವೆಬಾವಿ ಅಳವಡಿಕೆ",
            description = "New water source.",
            descriptionKn = "ಹೊಸ ನೀರಿನ ಮೂಲ.",
            location = "School Zone",
            locationKn = "ಶಾಲಾ ವಲಯ",
            budget = "₹1,20,000",
            status = "Completed",
            progress = 1.0f,
            completionDate = "10 Nov 2024",
            imageUrl = "https://images.unsplash.com/photo-1541604193435-225878996531?q=80&w=400",
            rating = 5
        )
    )
    GramaSuvidhaTheme {
        ProjectListScreen(
            projects = mockProjects,
            onProjectClick = {},
            onReportIssue = {},
            onRatingChange = { _, _ -> },
            onLanguageToggle = {}
        )
    }
}
