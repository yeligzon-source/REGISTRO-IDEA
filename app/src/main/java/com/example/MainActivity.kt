package com.example

import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RegistroEntity
import com.example.ui.AuthScreen
import com.example.ui.LanguageManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.RegistroViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val viewModel: RegistroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.init(this)
        enableEdgeToEdge()
        setContent {
            val isLightMode by viewModel.isLightMode.collectAsStateWithLifecycle()
            ThemeManager.isLightMode = isLightMode
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

// Accent palette models
data class AccentTheme(
    val name: String,
    val primary: Color,
    val glowing: Color,
    val gradient: Brush
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RegistroViewModel) {
    val context = LocalContext.current
    
    // Core Auth State check
    val loggedInUserEmail by viewModel.loggedInUserEmail.collectAsStateWithLifecycle()
    if (loggedInUserEmail == null) {
        AuthScreen(viewModel = viewModel)
        return
    }

    // Core State variables
    val registros by viewModel.filteredRegistros.collectAsStateWithLifecycle()
    val allRegistros by viewModel.allRegistros.collectAsStateWithLifecycle()
    val totalRecords by viewModel.totalCount.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryDistribution.collectAsStateWithLifecycle()
    
    val currentUserName by viewModel.loggedInUserName.collectAsStateWithLifecycle()
    val accentIndex by viewModel.accentColorIndex.collectAsStateWithLifecycle()
    
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    
    val selectedLeader by viewModel.selectedLeaderFilter.collectAsStateWithLifecycle()
    val selectedNetwork by viewModel.selectedNetworkFilter.collectAsStateWithLifecycle()
    val uniqueLeaders by viewModel.uniqueLeaders.collectAsStateWithLifecycle()
    val uniqueNetworks by viewModel.uniqueNetworks.collectAsStateWithLifecycle()

    // Screen State
    var currentTab by remember { mutableStateOf("bitacora") } // "bitacora" or "perfil"
    var showCreateDialog by remember { mutableStateOf(false) }

    // Navigation and styling setup based on color selection
    val accentThemes = listOf(
        AccentTheme(
            "Violeta Imperial", 
            VioletPrimary, 
            VioletGlowing, 
            Brush.horizontalGradient(listOf(VioletPrimary, VioletGlowing))
        ),
        AccentTheme(
            "Rosa Crepúsculo", 
            CoralPrimary, 
            CoralGlowing, 
            Brush.horizontalGradient(listOf(CoralPrimary, CoralGlowing))
        ),
        AccentTheme(
            "Esmeralda Menta", 
            EmeraldPrimary, 
            EmeraldGlowing, 
            Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldGlowing))
        ),
        AccentTheme(
            "Azul Cósmico", 
            AzurePrimary, 
            AzureGlowing, 
            Brush.horizontalGradient(listOf(AzurePrimary, AzureGlowing))
        )
    )
    
    val currentAccent = accentThemes.getOrElse(accentIndex) { accentThemes[0] }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        containerColor = BgDark,
        bottomBar = {
            Column {
                HorizontalDivider(color = BorderDark, thickness = 1.dp)
                NavigationBar(
                    containerColor = BgCardDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == "bitacora",
                        onClick = { currentTab = "bitacora" },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = currentAccent.primary,
                            selectedTextColor = currentAccent.primary,
                            indicatorColor = currentAccent.primary.copy(alpha = 0.15f),
                            unselectedIconColor = SubtitleDark,
                            unselectedTextColor = SubtitleDark
                        ),
                        icon = { Icon(Icons.Default.Home, contentDescription = LanguageManager.t("tab_bitacora")) },
                        label = { Text(LanguageManager.t("tab_bitacora"), fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_tab_bitacora")
                    )
                    NavigationBarItem(
                        selected = currentTab == "reportes",
                        onClick = { currentTab = "reportes" },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = currentAccent.primary,
                            selectedTextColor = currentAccent.primary,
                            indicatorColor = currentAccent.primary.copy(alpha = 0.15f),
                            unselectedIconColor = SubtitleDark,
                            unselectedTextColor = SubtitleDark
                        ),
                        icon = { Icon(Icons.Default.DateRange, contentDescription = LanguageManager.t("tab_reportes")) },
                        label = { Text(LanguageManager.t("tab_reportes"), fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_tab_reportes")
                    )
                    NavigationBarItem(
                        selected = currentTab == "perfil",
                        onClick = { currentTab = "perfil" },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = currentAccent.primary,
                            selectedTextColor = currentAccent.primary,
                            indicatorColor = currentAccent.primary.copy(alpha = 0.15f),
                            unselectedIconColor = SubtitleDark,
                            unselectedTextColor = SubtitleDark
                        ),
                        icon = { Icon(Icons.Default.Person, contentDescription = LanguageManager.t("tab_perfil")) },
                        label = { Text(LanguageManager.t("tab_perfil"), fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_tab_perfil")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == "bitacora") {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = currentAccent.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("add_record_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = LanguageManager.t("new_member_btn"),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background ambient glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = currentAccent.primary.copy(alpha = 0.08f),
                    radius = size.width / 1.5f,
                    center = Offset(size.width, 0f)
                )
                drawCircle(
                    color = currentAccent.glowing.copy(alpha = 0.05f),
                    radius = size.width / 2f,
                    center = Offset(0f, size.height)
                )
            }

            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    "bitacora" -> {
                        BitacoraTab(
                            registros = registros,
                            totalRecords = totalRecords,
                            query = query,
                            onQueryChange = { viewModel.searchQuery.value = it },
                            selectedFilter = selectedFilter,
                            onFilterChange = { viewModel.selectedCategoryFilter.value = it },
                            selectedLeader = selectedLeader,
                            onLeaderChange = { viewModel.selectedLeaderFilter.value = it },
                            uniqueLeaders = uniqueLeaders,
                            selectedNetwork = selectedNetwork,
                            onNetworkChange = { viewModel.selectedNetworkFilter.value = it },
                            uniqueNetworks = uniqueNetworks,
                            currentAccent = currentAccent,
                            userName = currentUserName,
                            onDelete = { id -> 
                                viewModel.deleteRegistro(id)
                                Toast.makeText(context, "Registro eliminado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    "reportes" -> {
                        ReportesTab(
                            allRegistros = allRegistros,
                            uniqueLeaders = uniqueLeaders,
                            uniqueNetworks = uniqueNetworks,
                            currentAccent = currentAccent
                        )
                    }
                    "perfil" -> {
                        PerfilTab(
                            userName = currentUserName,
                            userEmail = loggedInUserEmail ?: "",
                            totalRecords = totalRecords,
                            categoriesStats = categoryStats,
                            accentIndex = accentIndex,
                            accentThemes = accentThemes,
                            onUpdateProfile = { name, email, colorIdx ->
                                viewModel.updateProfile(name, email, colorIdx)
                                Toast.makeText(context, "Perfil guardado correctamente", Toast.LENGTH_SHORT).show()
                            },
                            onLogout = {
                                viewModel.logout()
                                Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                            },
                            onToggleLightMode = { isLight ->
                                viewModel.toggleLightMode(isLight)
                            },
                            currentAccent = currentAccent
                        )
                    }
                }
            }
        }
    }

    // Modal Create / Edit Log Form
    if (showCreateDialog) {
        CreateRegistroDialog(
            viewModel = viewModel,
            currentAccent = currentAccent,
            onDismiss = { showCreateDialog = false }
        )
    }
}

// ==========================================
// BITACORA TAB SUB-VIEW
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitacoraTab(
    registros: List<RegistroEntity>,
    totalRecords: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    selectedLeader: String,
    onLeaderChange: (String) -> Unit,
    uniqueLeaders: List<String>,
    selectedNetwork: String,
    onNetworkChange: (String) -> Unit,
    uniqueNetworks: List<String>,
    currentAccent: AccentTheme,
    userName: String,
    onDelete: (Int) -> Unit
) {
    val categories = listOf("Todos", "Seguimiento", "Univ. Vida", "Bautismo", "Capacitación", "Célula", "Otros")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Welcome and Stats Card Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola, $userName 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Diario de registros y miembros",
                    fontSize = 14.sp,
                    color = SubtitleDark
                )
            }

            // Quick Stats glassmorphic bubble
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(currentAccent.primary.copy(alpha = 0.12f))
                    .border(1.dp, currentAccent.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalRecords",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentAccent.primary
                    )
                    Text(
                        text = "Registros",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnBgDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Styled Search TextField
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar miembros por nombre...", color = SubtitleDark) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = SubtitleDark) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = currentAccent.primary,
                unfocusedBorderColor = BorderDark,
                focusedContainerColor = BgCardDark,
                unfocusedContainerColor = BgCardDark,
                cursorColor = currentAccent.primary,
                focusedPlaceholderColor = SubtitleDark,
                unfocusedPlaceholderColor = SubtitleDark
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            singleLine = true
        )

        // Dropdowns row for dynamic Leader and Network sorting/grouping filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Leader Dropdown Select
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BgCardDark,
                        contentColor = OnBgDark
                    ),
                    border = BorderStroke(1.dp, if (selectedLeader != "Todos") currentAccent.primary else BorderDark),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedLeader == "Todos") "Líder: Todos" else "Líder: $selectedLeader",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedLeader != "Todos") currentAccent.primary else OnBgDark
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (selectedLeader != "Todos") currentAccent.primary else SubtitleDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(BgCardDark).border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Filtrar Líder: Todos", fontSize = 12.sp, color = OnBgDark) },
                        onClick = {
                            onLeaderChange("Todos")
                            expanded = false
                        }
                    )
                    uniqueLeaders.forEach { leader ->
                        DropdownMenuItem(
                            text = { Text(leader, fontSize = 12.sp, color = OnBgDark) },
                            onClick = {
                                onLeaderChange(leader)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Network Dropdown Select
            Box(modifier = Modifier.weight(1f)) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BgCardDark,
                        contentColor = OnBgDark
                    ),
                    border = BorderStroke(1.dp, if (selectedNetwork != "Todos") currentAccent.primary else BorderDark),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedNetwork == "Todos") "Red: Todas" else "Red: $selectedNetwork",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedNetwork != "Todos") currentAccent.primary else OnBgDark
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (selectedNetwork != "Todos") currentAccent.primary else SubtitleDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(BgCardDark).border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Filtrar Red: Todas", fontSize = 12.sp, color = OnBgDark) },
                        onClick = {
                            onNetworkChange("Todos")
                            expanded = false
                        }
                    )
                    uniqueNetworks.forEach { network ->
                        DropdownMenuItem(
                            text = { Text(network, fontSize = 12.sp, color = OnBgDark) },
                            onClick = {
                                onNetworkChange(network)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Category Filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedFilter == category
                val chipBg = if (isSelected) currentAccent.primary else BgCardDark
                val borderCol = if (isSelected) currentAccent.primary else BorderDark
                val textCol = if (isSelected) Color.White else SubtitleDark

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(chipBg)
                        .border(1.dp, borderCol, RoundedCornerShape(30.dp))
                        .clickable { onFilterChange(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (category != "Todos") {
                            Text(
                                text = getCategoryEmoji(category),
                                modifier = Modifier.padding(end = 4.dp),
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = category,
                            color = textCol,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Header Title for list
        Text(
            text = if (selectedFilter == "Todos") "Últimos registrados" else "Miembros de $selectedFilter",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnBgDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Records List Room Observation
        if (registros.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sin registros",
                        tint = SubtitleDark.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aún no tienes registros guardados.",
                        color = SubtitleDark,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Presiona '+' abajo para registrar tu primer miembro.",
                        color = SubtitleDark.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(registros, key = { it.id }) { registro ->
                    RegistroItemRow(
                        registro = registro,
                        currentAccent = currentAccent,
                        onDelete = { onDelete(registro.id) }
                    )
                }
            }
        }
    }
}

// Helper to resolve icon categories mapping
fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Célula" -> "🏠"
        "Capacitación" -> "🚀"
        "Bautismo" -> "💧"
        "Univ. Vida" -> "🎓"
        "Seguimiento" -> "📞"
        else -> "📝"
    }
}

// Single item container layout for registros List
@Composable
fun RegistroItemRow(
    registro: RegistroEntity,
    currentAccent: AccentTheme,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(registro.timestamp))
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = BgCardDark),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (expanded) currentAccent.primary else BorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(currentAccent.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getCategoryEmoji(registro.category), fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${registro.nombre} ${registro.apellido}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = registro.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentAccent.primary
                            )
                            Text(
                                text = "  •  $formattedDate",
                                fontSize = 11.sp,
                                color = SubtitleDark
                            )
                        }

                        // Badges for leader and network next to/under name
                        if (registro.liderAsignado.isNotBlank() || registro.redAsignada.isNotBlank()) {
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (registro.liderAsignado.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(currentAccent.primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "👤 Líd: ${registro.liderAsignado}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentAccent.primary
                                        )
                                    }
                                }
                                if (registro.redAsignada.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF4285F4).copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "🕸️ ${registro.redAsignada}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF66B2FF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Delete Icon & Chevron dropdown indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar registro",
                            tint = CoralPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Ver detalles",
                        tint = SubtitleDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body contact details
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = SubtitleDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = registro.telefono, fontSize = 13.sp, color = OnBgDark)
                    
                    if (registro.cedula.isNotBlank()) {
                        Text(text = "   |   ID: ${registro.cedula}", fontSize = 13.sp, color = SubtitleDark)
                    }
                }
                
                if (registro.direccionCompleta.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SubtitleDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = registro.direccionCompleta, fontSize = 12.sp, color = SubtitleDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Progress Stages Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Milestone icons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Llamadas
                    MilestoneBadge(label = "📞", active = registro.llamada1 || registro.llamada2 || registro.llamada3, currentAccent = currentAccent)
                    // Visita
                    MilestoneBadge(label = "🏠 Visita", active = registro.visita, currentAccent = currentAccent)
                    // Univ Vida
                    MilestoneBadge(label = "🎓 Univ", active = registro.universidadVida, currentAccent = currentAccent)
                    // Bautismo
                    MilestoneBadge(label = "💧 Baut", active = registro.bautismo, currentAccent = currentAccent)
                    // Capacitación
                    MilestoneBadge(label = "🚀 Cap", active = registro.capacitacionDestino, currentAccent = currentAccent)
                    // Célula
                    MilestoneBadge(label = "🏠 Cél", active = registro.celula, currentAccent = currentAccent)
                }

                if (registro.quienInvito.isNotBlank()) {
                    Text(
                        text = "Inv: ${registro.quienInvito}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = currentAccent.glowing,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Expandable bottom details panel for full church profiles
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "INFORMACIÓN DETALLADA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentAccent.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Estado Civil", fontSize = 10.sp, color = SubtitleDark)
                            Text(
                                text = if (registro.estadoCivil.isNotBlank()) registro.estadoCivil else "No especificado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (registro.cumpleanos.isNotBlank() && registro.cumpleanos != "mm/dd/aaaa" && registro.cumpleanos != "mm/dd/yyyy") {
                            val age = calculateAge(registro.cumpleanos)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Fecha de Nacimiento", fontSize = 10.sp, color = SubtitleDark)
                                Text(
                                    text = "${registro.cumpleanos} (${age ?: "?"} años)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (registro.celula) {
                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                        Text(
                            text = "INFORMACIÓN DE CÉLULA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentAccent.glowing,
                            letterSpacing = 0.5.sp
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Nombre/Célula", fontSize = 10.sp, color = SubtitleDark)
                                Text(
                                    text = if (registro.nombreCelula.isNotBlank()) registro.nombreCelula else "Asiste",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rol en la Célula", fontSize = 10.sp, color = SubtitleDark)
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(currentAccent.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (registro.rolCelula.isNotBlank()) registro.rolCelula else "Asistente",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentAccent.primary
                                    )
                                }
                            }
                        }

                        if (registro.territorioCelula.isNotBlank()) {
                            Column {
                                Text("Territorio / Ubicación", fontSize = 10.sp, color = SubtitleDark)
                                Text(registro.territorioCelula, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        if (registro.fechaIngresoCelula.isNotBlank() && registro.fechaIngresoCelula != "mm/dd/aaaa" && registro.fechaIngresoCelula != "mm/dd/yyyy") {
                            Column {
                                Text("Asiste desde el", fontSize = 10.sp, color = SubtitleDark)
                                Text(registro.fechaIngresoCelula, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    if (registro.liderAsignado.isNotBlank() || registro.redAsignada.isNotBlank()) {
                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (registro.liderAsignado.isNotBlank()) {
                                Column {
                                    Text("Líder Asignado", fontSize = 10.sp, color = SubtitleDark)
                                    Text(registro.liderAsignado, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            if (registro.redAsignada.isNotBlank()) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Red Asignada", fontSize = 10.sp, color = SubtitleDark)
                                    Text(registro.redAsignada, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneBadge(
    label: String,
    active: Boolean,
    currentAccent: AccentTheme
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) currentAccent.primary.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (active) currentAccent.primary else BorderDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else SubtitleDark.copy(alpha = 0.6f)
        )
    }
}


// ==========================================
// PROFILE & SETTINGS TAB SUB-VIEW
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilTab(
    userName: String,
    userEmail: String,
    totalRecords: Int,
    categoriesStats: Map<String, Int>,
    accentIndex: Int,
    accentThemes: List<AccentTheme>,
    onUpdateProfile: (String, String, Int) -> Unit,
    onLogout: () -> Unit,
    onToggleLightMode: (Boolean) -> Unit,
    currentAccent: AccentTheme
) {
    var editName by remember { mutableStateOf(userName) }
    var editEmail by remember { mutableStateOf(userEmail) }
    var isEditing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = LanguageManager.t("tab_perfil"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Profile details Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, shape = RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = BgCardDark),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(currentAccent.primary.copy(alpha = 0.2f))
                            .border(2.dp, currentAccent.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "O",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentAccent.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isEditing) {
                        Text(
                            text = userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = userEmail,
                            fontSize = 13.sp,
                            color = SubtitleDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = LanguageManager.t("authorized_leader"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentAccent.glowing
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isEditing = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(LanguageManager.t("edit_my_data"), fontSize = 12.sp, color = OnBgDark)
                            }
                            Button(
                                onClick = onLogout,
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("logout_button")
                            ) {
                                Text(LanguageManager.t("logout"), fontSize = 12.sp, color = Color.White)
                            }
                        }
                    } else {
                        // Edit Mode Layout
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(LanguageManager.t("login_name")) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = currentAccent.primary,
                                unfocusedBorderColor = BorderDark,
                                focusedContainerColor = BgCardDark,
                                unfocusedContainerColor = BgCardDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text(LanguageManager.t("login_email")) },
                            colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = currentAccent.primary,
                                    unfocusedBorderColor = BorderDark,
                                    focusedContainerColor = BgCardDark,
                                    unfocusedContainerColor = BgCardDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        editName = userName
                                        editEmail = userEmail
                                        isEditing = false
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(LanguageManager.t("cancel"), color = OnBgDark)
                                }
                                Button(
                                    onClick = {
                                        onUpdateProfile(editName, editEmail, accentIndex)
                                        isEditing = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentAccent.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(LanguageManager.t("save"), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Centralized System Theme select Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCardDark),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = LanguageManager.t("accent_color"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = LanguageManager.t("accent_desc"),
                            fontSize = 12.sp,
                            color = SubtitleDark,
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            accentThemes.forEachIndexed { idx, theme ->
                                val isSelected = idx == accentIndex
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(theme.primary)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onUpdateProfile(userName, userEmail, idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Modo Claro Switch Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCardDark),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LanguageManager.t("light_mode"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBgDark
                            )
                            Text(
                                text = LanguageManager.t("light_mode_desc"),
                                fontSize = 11.sp,
                                color = SubtitleDark,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Switch(
                            checked = ThemeManager.isLightMode,
                            onCheckedChange = { isLight ->
                                onToggleLightMode(isLight)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = currentAccent.primary,
                                uncheckedThumbColor = SubtitleDark,
                                uncheckedTrackColor = BorderDark
                            )
                        )
                    }
                }
            }

            // NEW: World Language Selector Card
            item {
                val context = LocalContext.current
                var languageExpanded by remember { mutableStateOf(false) }
                val currentLanguageName = LanguageManager.supportedLanguages.find { it.first == LanguageManager.currentLangCode }?.second ?: "⚙️ Sistema"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCardDark),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = LanguageManager.t("lang_title"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = LanguageManager.t("lang_desc"),
                            fontSize = 11.sp,
                            color = SubtitleDark,
                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { languageExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = BgDark,
                                    contentColor = OnBgDark
                                ),
                                border = BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentLanguageName,
                                        fontSize = 13.sp,
                                        color = OnBgDark
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = SubtitleDark
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = languageExpanded,
                                onDismissRequest = { languageExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(BgCardDark)
                                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            ) {
                                LanguageManager.supportedLanguages.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = { Text(name, fontSize = 13.sp, color = OnBgDark) },
                                        onClick = {
                                            LanguageManager.setLanguage(context, code)
                                            languageExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }


// ==========================================
// REPORT GENERATOR TAB SUB-VIEW
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesTab(
    allRegistros: List<RegistroEntity>,
    uniqueLeaders: List<String>,
    uniqueNetworks: List<String>,
    currentAccent: AccentTheme
) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var reportType by remember { mutableStateOf("Total") } // "Total" or "Lider"
    var selectedLeader by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf("Todos") } // Network filtering
    var selectedFormat by remember { mutableStateOf("Completo") } // "Completo", "Estadístico", "Contactos"
    var reportText by remember { mutableStateOf("") }

    // Set initial leader if empty
    LaunchedEffect(uniqueLeaders) {
        if (selectedLeader.isBlank() && uniqueLeaders.isNotEmpty()) {
            selectedLeader = uniqueLeaders.first()
        }
    }

    var leaderDropdownExpanded by remember { mutableStateOf(false) }
    var networkDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title block
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = LanguageManager.t("rep_generator"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = OnBgDark
                )
                Text(
                    text = LanguageManager.t("app_description"),
                    fontSize = 12.sp,
                    color = SubtitleDark,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCardDark),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = LanguageManager.t("rep_filters"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentAccent.primary,
                        letterSpacing = 0.5.sp
                    )

                    // 1. REPORT TYPE SELECTOR
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = LanguageManager.t("rep_scope"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBgDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Option Total
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (reportType == "Total") currentAccent.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (reportType == "Total") currentAccent.primary else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { reportType = "Total" }
                            ) {
                                Text(
                                    text = LanguageManager.t("rep_total"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reportType == "Total") currentAccent.primary else SubtitleDark
                                )
                            }

                            // Option Por Líder
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (reportType == "Lider") currentAccent.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(1.dp, if (reportType == "Lider") currentAccent.primary else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { reportType = "Lider" }
                            ) {
                                Text(
                                    text = LanguageManager.t("rep_by_leader"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (reportType == "Lider") currentAccent.primary else SubtitleDark
                                )
                            }
                        }
                    }

                    // 2. LEADER DROPDOWN (ONLY SHOWS IF reportType IS "Lider")
                    if (reportType == "Lider") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = LanguageManager.t("rep_select_leader"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBgDark
                            )
                            if (uniqueLeaders.isEmpty()) {
                                Text(
                                    text = LanguageManager.t("no_leaders_found"),
                                    fontSize = 11.sp,
                                    color = CoralPrimary.copy(alpha = 0.8f)
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { leaderDropdownExpanded = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = BgDark,
                                            contentColor = OnBgDark
                                        ),
                                        border = BorderStroke(1.dp, BorderDark),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (selectedLeader.isNotBlank()) "👤 $selectedLeader" else LanguageManager.t("select_leader_placeholder"),
                                                fontSize = 13.sp,
                                                color = OnBgDark
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = SubtitleDark
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = leaderDropdownExpanded,
                                        onDismissRequest = { leaderDropdownExpanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .background(BgCardDark)
                                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                    ) {
                                        uniqueLeaders.forEach { leader ->
                                            DropdownMenuItem(
                                                text = { Text(leader, fontSize = 13.sp, color = OnBgDark) },
                                                onClick = {
                                                    selectedLeader = leader
                                                    leaderDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. NETWORK FILTER (OPTIONAL)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = LanguageManager.t("rep_network"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBgDark
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { networkDropdownExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = BgDark,
                                    contentColor = OnBgDark
                                ),
                                border = BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedNetwork == "Todos") "🕸️ " + LanguageManager.t("rep_all_networks") else "🕸️ $selectedNetwork",
                                        fontSize = 13.sp,
                                        color = OnBgDark
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = SubtitleDark
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = networkDropdownExpanded,
                                onDismissRequest = { networkDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(BgCardDark)
                                    .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text(LanguageManager.t("rep_all_networks"), fontSize = 13.sp, color = OnBgDark) },
                                    onClick = {
                                        selectedNetwork = "Todos"
                                        networkDropdownExpanded = false
                                    }
                                )
                                uniqueNetworks.forEach { network ->
                                    DropdownMenuItem(
                                        text = { Text(network, fontSize = 13.sp, color = OnBgDark) },
                                        onClick = {
                                            selectedNetwork = network
                                            networkDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4. FORMAT SELECTOR
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = LanguageManager.t("rep_format"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBgDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Completo", "Estadístico", "Contactos").forEach { fmt ->
                                val active = selectedFormat == fmt
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) currentAccent.primary.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(1.dp, if (active) currentAccent.primary else BorderDark, RoundedCornerShape(8.dp))
                                        .clickable { selectedFormat = fmt }
                                ) {
                                    Text(
                                        text = when(fmt) {
                                            "Completo" -> LanguageManager.t("rep_detallado")
                                            "Estadístico" -> LanguageManager.t("rep_estadistico")
                                            else -> LanguageManager.t("rep_contactos")
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) currentAccent.primary else SubtitleDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 5. GENERATE BUTTON
                    Button(
                        onClick = {
                            reportText = buildCustomReportString(
                                allRegistros,
                                reportType,
                                selectedLeader,
                                selectedNetwork,
                                selectedFormat
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = currentAccent.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = LanguageManager.t("rep_gen_btn"),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Output Result Card
        if (reportText.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCardDark),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, currentAccent.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageManager.t("rep_output_title"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentAccent.primary,
                                letterSpacing = 0.5.sp
                            )

                            Text(
                                text = LanguageManager.t("rep_clear"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = CoralPrimary,
                                modifier = Modifier.clickable { reportText = "" }
                            )
                        }

                        // Editable output report text area
                        OutlinedTextField(
                            value = reportText,
                            onValueChange = { reportText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 350.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = OnBgDark
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark,
                                focusedBorderColor = BorderDark,
                                unfocusedBorderColor = BorderDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Copy to clipboard action button
                            Button(
                                onClick = {
                                    clipboardManager.setPrimaryClip(
                                        ClipData.newPlainText("Reporte Registro Idea", reportText)
                                    )
                                    val copyToastMsg = if (LanguageManager.getActiveLangCode() == "es") "Reporte copiado" else "Report copied"
                                    Toast.makeText(context, copyToastMsg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = currentAccent.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(LanguageManager.t("rep_copy"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Share Action Button
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, reportText)
                                    }
                                    val chooserTitle = if (LanguageManager.getActiveLangCode() == "es") "Compartir Reporte" else "Share Report"
                                    context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnBgDark),
                                border = BorderStroke(1.dp, BorderDark),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = OnBgDark, modifier = Modifier.size(16.dp))
                                    Text(LanguageManager.t("rep_share"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Empty Placeholder Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCardDark),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(currentAccent.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = currentAccent.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = LanguageManager.t("rep_ready_title"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBgDark
                        )
                        Text(
                            text = LanguageManager.t("rep_ready_desc"),
                            fontSize = 11.sp,
                            color = SubtitleDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Custom Report Formatter
fun buildCustomReportString(
    registros: List<RegistroEntity>,
    type: String,
    selectedLeader: String,
    selectedNetwork: String,
    selectedFormat: String
): String {
    val filtered = registros.filter { item ->
        val matchesLeader = type == "Total" || item.liderAsignado == selectedLeader
        val matchesNetwork = selectedNetwork == "Todos" || item.redAsignada == selectedNetwork
        matchesLeader && matchesNetwork
    }

    val sb = java.lang.StringBuilder()
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    val currentDateText = sdf.format(java.util.Date())

    // local translation helper
    fun localT(es: String, en: String, pt: String, fr: String = en, it: String = en, de: String = en): String {
        return when(LanguageManager.getActiveLangCode()) {
            "es" -> es
            "pt" -> pt
            "fr" -> fr
            "it" -> it
            "de" -> de
            "en" -> en
            else -> en
        }
    }

    sb.append("📋 *${localT("REPORTE - REGISTRO IDEA", "REPORT - REGISTRO IDEA", "RELATÓRIO - REGISTRO IDEA").uppercase()}*\n")
    sb.append("📅 *${localT("Generado el:", "Generated on:", "Gerado em:")}* $currentDateText\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

    sb.append("📊 *${localT("RESUMEN DE BÚSQUEDA:", "SEARCH SUMMARY:", "RESUMO DA BUSCA:")}*\n")
    sb.append("• *${localT("Tipo:", "Type:", "Tipo:")}* ${if (type == "Total") LanguageManager.t("rep_total") else "${LanguageManager.t("rep_by_leader")} ($selectedLeader)"}\n")
    sb.append("• *${localT("Red:", "Network:", "Rede:")}* ${if (selectedNetwork == "Todos") LanguageManager.t("rep_all_networks") else selectedNetwork}\n")
    sb.append("• *${localT("Registros Encontrados:", "Records Found:", "Registros Encontrados:")}* ${filtered.size}\n\n")

    if (filtered.isEmpty()) {
        sb.append("⚠️ _${localT("No se encontraron registros que coincidan con los filtros seleccionados.", "No records found matching selected filters.", "Nenhum registro encontrado correspondente aos filtros selecionados.")}_\n")
        return sb.toString()
    }

    // List categories stats
    val catDistribution = filtered.groupBy { it.category }.mapValues { it.value.size }
    sb.append("👥 *${localT("CATEGORÍAS:", "CATEGORIES:", "CATEGORIAS:")}*\n")
    catDistribution.forEach { (cat, count) ->
        sb.append("  ▪️ *${cat}:* $count\n")
    }
    sb.append("\n")

    // List cell statistics
    val inCell = filtered.count { it.celula }
    val outCell = filtered.size - inCell
    sb.append("🏡 *${localT("CÉLULAS:", "CELL GROUPS:", "CÉLULAS:")}*\n")
    sb.append("  ▪️ *${localT("En Célula:", "In Cell Group:", "Em Célula:")}* $inCell ${localT("miembros", "members", "membros")}\n")
    sb.append("  ▪️ *${localT("Sin Célula:", "No Cell Group:", "Sem Célula:")}* $outCell ${localT("miembros", "members", "membros")}\n\n")

    if (selectedFormat == "Estadístico") {
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("✨ ${localT("Fin del Reporte Estadístico. Generado con REGISTRO IDEA.", "End of Statistical Report. Generated with REGISTRO IDEA.", "Fim do Relatório Estatístico. Gerado com REGISTRO IDEA.")}\n")
        return sb.toString()
    }

    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("👥 *${localT("DETALLE DE REGISTROS", "RECORDS DETAIL", "DETALHES DOS REGISTROS")} (${filtered.size}):*\n\n")

    filtered.forEachIndexed { idx, reg ->
        if (selectedFormat == "Contactos") {
            sb.append("${idx + 1}. *${reg.nombre} ${reg.apellido}*\n")
            sb.append("   📞 *${localT("Tel:", "Phone:", "Tel:")}* ${if (reg.telefono.isNotBlank()) reg.telefono else localT("Sin especificar", "Not specified", "Não especificado")}\n")
            if (reg.liderAsignado.isNotBlank()) {
                sb.append("   👤 *${localT("Líder:", "Leader:", "Líder:")}* ${reg.liderAsignado}\n")
            }
            sb.append("\n")
        } else {
            sb.append("${idx + 1}. *${reg.nombre} ${reg.apellido}* (${reg.category})\n")
            if (reg.telefono.isNotBlank()) {
                sb.append("   📞 *${localT("Teléfono:", "Phone:", "Telefone:")}* ${reg.telefono}\n")
            }
            if (reg.cedula.isNotBlank()) {
                sb.append("   🆔 *${localT("Cédula/ID:", "National ID:", "Cédula/ID:")}* ${reg.cedula}\n")
            }
            if (reg.estadoCivil.isNotBlank()) {
                sb.append("   💍 *${localT("Est. Civil:", "Civil Status:", "Est. Civil:")}* ${reg.estadoCivil}\n")
            }
            if (reg.cumpleanos.isNotBlank() && reg.cumpleanos != "mm/dd/aaaa" && reg.cumpleanos != "mm/dd/yyyy") {
                sb.append("   🎂 *${localT("Cumpleaños:", "Birthday:", "Aniversário:")}* ${reg.cumpleanos}\n")
            }
            if (reg.celula) {
                sb.append("   🏠 *${localT("Célula:", "Cell Group:", "Célula:")}* ${localT("Sí", "Yes", "Sim")}\n")
                if (reg.nombreCelula.isNotBlank()) {
                    sb.append("     ▪️ *${localT("Nombre:", "Name:", "Nome:")}* ${reg.nombreCelula}\n")
                }
                if (reg.rolCelula.isNotBlank()) {
                    sb.append("     ▪️ *${localT("Rol:", "Role:", "Cargo:")}* ${reg.rolCelula}\n")
                }
                if (reg.territorioCelula.isNotBlank()) {
                    sb.append("     ▪️ *${localT("Región:", "Region:", "Região:")}* ${reg.territorioCelula}\n")
                }
            } else {
                sb.append("   🏠 *${localT("Asiste a Célula:", "Attends Cell Group:", "Frequenta Célula:")}* ${localT("No", "No", "Não")}\n")
            }
            if (reg.liderAsignado.isNotBlank()) {
                sb.append("   👤 *${localT("Líder Asignado:", "Assigned Leader:", "Líder Designado:")}* ${reg.liderAsignado}\n")
            }
            if (reg.redAsignada.isNotBlank()) {
                sb.append("   🕸️ *${localT("Red:", "Network:", "Rede:")}* ${reg.redAsignada}\n")
            }
            if (reg.quienInvito.isNotBlank()) {
                sb.append("   🤝 *${localT("Invitado por:", "Invited by:", "Convidado por:")}* ${reg.quienInvito}\n")
            }
            sb.append("\n")
        }
    }

    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("✨ ${localT("Generado automáticamente por REGISTRO IDEA.", "Automatically generated by REGISTRO IDEA.", "Gerado automaticamente por REGISTRO IDEA.")}")
    return sb.toString()
}


    // ==========================================
    // CUSTOM INTENSE CALENDAR DATE PICKER & FORM DIALOGUE
    // ==========================================
    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun CreateRegistroDialog(
        viewModel: RegistroViewModel,
        currentAccent: AccentTheme,
        onDismiss: () -> Unit
    ) {
        val context = LocalContext.current

        // 1. INFORMACIÓN PERSONAL
        var nombre by remember { mutableStateOf("") }
        var apellido by remember { mutableStateOf("") }
        var telefono by remember { mutableStateOf("") }
        var cedula by remember { mutableStateOf("") }
        var cumpleanos by remember { mutableStateOf("mm/dd/aaaa") }
        var direccionCompleta by remember { mutableStateOf("") }
        var quienInvito by remember { mutableStateOf("") }

        // 2. SEGUIMIENTO
        var llamada1 by remember { mutableStateOf(false) }
        var llamada2 by remember { mutableStateOf(false) }
        var llamada3 by remember { mutableStateOf(false) }
        var visita by remember { mutableStateOf(false) }
        var nombreLiderVisito by remember { mutableStateOf("") }

        // 3. PROCESO CREYENTE
        var fechaConversion by remember { mutableStateOf("mm/dd/aaaa") }
        var universidadVida by remember { mutableStateOf(false) }
        var universidadVidaInicio by remember { mutableStateOf("mm/dd/aaaa") }
        var universidadVidaFin by remember { mutableStateOf("mm/dd/aaaa") }
        var bautismo by remember { mutableStateOf(false) }
        var fechaBautismo by remember { mutableStateOf("mm/dd/aaaa") }
        var bautismoOtraCongregacion by remember { mutableStateOf(false) }
        var nombreCongregacion by remember { mutableStateOf("") }
        var capacitacionDestino by remember { mutableStateOf(false) }
        var capacitacionDestinoInicio by remember { mutableStateOf("mm/dd/aaaa") }
        var capacitacionDestinoFin by remember { mutableStateOf("mm/dd/aaaa") }
        var capacitacionDestinoNivel1 by remember { mutableStateOf(false) }
        var capacitacionDestinoNivel2 by remember { mutableStateOf(false) }
        var capacitacionDestinoNivel3 by remember { mutableStateOf(false) }
        var celula by remember { mutableStateOf(false) }
        var nombreCelula by remember { mutableStateOf("") }
        var rolCelula by remember { mutableStateOf("Asistente") }
        var territorioCelula by remember { mutableStateOf("") }
        var fechaIngresoCelula by remember { mutableStateOf("mm/dd/aaaa") }
        var estadoCivil by remember { mutableStateOf("Soltero") }
        var liderAsignado by remember { mutableStateOf("") }
        var redAsignada by remember { mutableStateOf("") }

        val keyboardController = LocalSoftwareKeyboardController.current
        val dismissWithKeyboard = {
            keyboardController?.hide()
            onDismiss()
        }

        Dialog(onDismissRequest = dismissWithKeyboard) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                colors = CardDefaults.cardColors(containerColor = BgDark),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Text(
                        text = "Nuevo registro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // SECTION 1: INFORMACIÓN PERSONAL
                        item {
                            Text(
                                text = "INFORMACIÓN PERSONAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentAccent.primary,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = "Nombre",
                                    placeholder = "Nombre",
                                    currentAccent = currentAccent,
                                    required = true,
                                    modifier = Modifier.weight(1f)
                                )
                                FormTextField(
                                    value = apellido,
                                    onValueChange = { apellido = it },
                                    label = "Apellido",
                                    placeholder = "Apellido",
                                    currentAccent = currentAccent,
                                    required = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            FormTextField(
                                value = telefono,
                                onValueChange = { telefono = it },
                                label = "Teléfono celular",
                                placeholder = "Teléfono celular",
                                currentAccent = currentAccent,
                                required = true,
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SubtitleDark) }
                            )
                        }

                        item {
                            FormTextField(
                                value = cedula,
                                onValueChange = { cedula = it },
                                label = "Cédula / ID",
                                placeholder = "Cédula o ID de identidad",
                                currentAccent = currentAccent
                            )
                        }

                        item {
                            val computedAge = remember(cumpleanos) { calculateAge(cumpleanos) }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                FormDateField(
                                    value = cumpleanos,
                                    onValueChange = { cumpleanos = it },
                                    placeholder = "mm/dd/aaaa",
                                    label = "Fecha de Nacimiento",
                                    currentAccent = currentAccent
                                )
                                computedAge?.let { age ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(currentAccent.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Edad: $age años",
                                                color = currentAccent.primary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "(Se actualiza de forma automática cada año)",
                                            color = SubtitleDark,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            FormChoiceChips(
                                label = "Estado Civil",
                                options = listOf("Soltero", "Casado", "Divorciado", "Viudo"),
                                selectedOption = estadoCivil,
                                onOptionSelected = { estadoCivil = it },
                                currentAccent = currentAccent
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    FormTextField(
                                        value = liderAsignado,
                                        onValueChange = { liderAsignado = it },
                                        label = "Líder asignado",
                                        placeholder = "Líder a cargo",
                                        currentAccent = currentAccent,
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SubtitleDark) }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    FormTextField(
                                        value = redAsignada,
                                        onValueChange = { redAsignada = it },
                                        label = "Red asignada",
                                        placeholder = "Red de la persona",
                                        currentAccent = currentAccent,
                                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = SubtitleDark) }
                                    )
                                }
                            }
                        }

                        item {
                            FormTextField(
                                value = direccionCompleta,
                                onValueChange = { direccionCompleta = it },
                                label = "Dirección completa",
                                placeholder = "Dirección habitacional completa",
                                currentAccent = currentAccent,
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SubtitleDark) }
                            )
                        }

                        item {
                            FormTextField(
                                value = quienInvito,
                                onValueChange = { quienInvito = it },
                                label = "¿Quién lo invitó?",
                                placeholder = "Nombre del contacto que lo invitó",
                                currentAccent = currentAccent,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SubtitleDark) }
                            )
                        }

                        // SECTION 2: SEGUIMIENTO
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "SEGUIMIENTO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentAccent.primary,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            FormCheckboxRow(
                                checked = llamada1,
                                onCheckedChange = { llamada1 = it },
                                label = "Llamada 1",
                                currentAccent = currentAccent,
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                            )
                        }

                        item {
                            FormCheckboxRow(
                                checked = llamada2,
                                onCheckedChange = { llamada2 = it },
                                label = "Llamada 2",
                                currentAccent = currentAccent,
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                            )
                        }

                        item {
                            FormCheckboxRow(
                                checked = llamada3,
                                onCheckedChange = { llamada3 = it },
                                label = "Llamada 3",
                                currentAccent = currentAccent,
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormCheckboxRow(
                                    checked = visita,
                                    onCheckedChange = { visita = it },
                                    label = "Visita",
                                    currentAccent = currentAccent,
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                                )

                                AnimatedVisibility(
                                    visible = visita,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    FormTextField(
                                        value = nombreLiderVisito,
                                        onValueChange = { nombreLiderVisito = it },
                                        label = "Nombre del líder que visitó",
                                        placeholder = "Nombre del líder que visitó",
                                        currentAccent = currentAccent,
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SubtitleDark) }
                                    )
                                }
                            }
                        }

                        // SECTION 3: PROCESO CREYENTE
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "PROCESO CREYENTE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentAccent.primary,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            FormDateField(
                                value = fechaConversion,
                                onValueChange = { fechaConversion = it },
                                placeholder = "mm/dd/aaaa",
                                label = "1. Fecha de Conversión",
                                currentAccent = currentAccent
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormCheckboxRow(
                                    checked = universidadVida,
                                    onCheckedChange = { universidadVida = it },
                                    label = "2. Universidad de la Vida",
                                    currentAccent = currentAccent,
                                    leadingIcon = { Icon(Icons.Default.Menu, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                                )

                                AnimatedVisibility(
                                    visible = universidadVida,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FormDateField(
                                            value = universidadVidaInicio,
                                            onValueChange = { universidadVidaInicio = it },
                                            placeholder = "mm/dd/aaaa",
                                            label = "INICIO",
                                            currentAccent = currentAccent,
                                            modifier = Modifier.weight(1f)
                                        )
                                        FormDateField(
                                            value = universidadVidaFin,
                                            onValueChange = { universidadVidaFin = it },
                                            placeholder = "mm/dd/aaaa",
                                            label = "FINALIZACIÓN",
                                            currentAccent = currentAccent,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormCheckboxRow(
                                    checked = bautismo,
                                    onCheckedChange = { bautismo = it },
                                    label = "3. Bautismo",
                                    currentAccent = currentAccent,
                                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                                )

                                AnimatedVisibility(
                                    visible = bautismo,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        FormDateField(
                                            value = fechaBautismo,
                                            onValueChange = { fechaBautismo = it },
                                            placeholder = "mm/dd/aaaa",
                                            label = "FECHA DE BAUTISMO",
                                            currentAccent = currentAccent
                                        )
                                        
                                        FormCheckboxRow(
                                            checked = bautismoOtraCongregacion,
                                            onCheckedChange = { bautismoOtraCongregacion = it },
                                            label = "BAUTIZADO EN OTRA CONGREGACIÓN",
                                            currentAccent = currentAccent
                                        )

                                        if (bautismoOtraCongregacion) {
                                            FormTextField(
                                                value = nombreCongregacion,
                                                onValueChange = { nombreCongregacion = it },
                                                label = "Nombre de la congregación",
                                                placeholder = "Nombre de la congregación",
                                                currentAccent = currentAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormCheckboxRow(
                                    checked = capacitacionDestino,
                                    onCheckedChange = { capacitacionDestino = it },
                                    label = "4. Capacitación Destino",
                                    currentAccent = currentAccent,
                                    leadingIcon = { Icon(Icons.Default.AddCircle, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                                )

                                AnimatedVisibility(
                                    visible = capacitacionDestino,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            FormDateField(
                                                value = capacitacionDestinoInicio,
                                                onValueChange = { capacitacionDestinoInicio = it },
                                                placeholder = "mm/dd/aaaa",
                                                label = "INICIO",
                                                currentAccent = currentAccent,
                                                modifier = Modifier.weight(1f)
                                            )
                                            FormDateField(
                                                value = capacitacionDestinoFin,
                                                onValueChange = { capacitacionDestinoFin = it },
                                                placeholder = "mm/dd/aaaa",
                                                label = "FINALIZACIÓN",
                                                currentAccent = currentAccent,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = capacitacionDestinoNivel1,
                                                    onCheckedChange = { capacitacionDestinoNivel1 = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = currentAccent.primary)
                                                )
                                                Text("Nivel 1", color = OnBgDark, fontSize = 11.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = capacitacionDestinoNivel2,
                                                    onCheckedChange = { capacitacionDestinoNivel2 = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = currentAccent.primary)
                                                )
                                                Text("Nivel 2", color = OnBgDark, fontSize = 11.sp)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = capacitacionDestinoNivel3,
                                                    onCheckedChange = { capacitacionDestinoNivel3 = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = currentAccent.primary)
                                                )
                                                Text("Nivel 3", color = OnBgDark, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FormCheckboxRow(
                                    checked = celula,
                                    onCheckedChange = { celula = it },
                                    label = "5. Célula",
                                    currentAccent = currentAccent,
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = currentAccent.primary.copy(alpha = 0.8f)) }
                                )

                                AnimatedVisibility(
                                    visible = celula,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BgCardDark)
                                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        FormTextField(
                                            value = nombreCelula,
                                            onValueChange = { nombreCelula = it },
                                            label = "Nombre/Número de la Célula",
                                            placeholder = "Nombre o número identificador de la célula",
                                            currentAccent = currentAccent
                                        )

                                        FormChoiceChips(
                                            label = "Rol en la Célula",
                                            options = listOf("Líder", "Anfitrión", "Timoteo", "Asistente"),
                                            selectedOption = rolCelula,
                                            onOptionSelected = { rolCelula = it },
                                            currentAccent = currentAccent
                                        )

                                        FormTextField(
                                            value = territorioCelula,
                                            onValueChange = { territorioCelula = it },
                                            label = "Territorio de la Célula",
                                            placeholder = "Ubicación o territorio de la célula",
                                            currentAccent = currentAccent,
                                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SubtitleDark) }
                                        )

                                        FormDateField(
                                            value = fechaIngresoCelula,
                                            onValueChange = { fechaIngresoCelula = it },
                                            placeholder = "mm/dd/aaaa",
                                            label = "Fecha desde que asiste",
                                            currentAccent = currentAccent
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons representing image footer bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = dismissWithKeyboard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Regresar", color = OnBgDark)
                        }
                        Button(
                            onClick = {
                                if (nombre.isBlank() || apellido.isBlank() || telefono.isBlank()) {
                                    Toast.makeText(context, "Los campos Nombre, Apellido y Teléfono son obligatorios.", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.addRegistro(
                                        nombre = nombre,
                                        apellido = apellido,
                                        telefono = telefono,
                                        cedula = cedula,
                                        cumpleanos = cumpleanos,
                                        direccionCompleta = direccionCompleta,
                                        quienInvito = quienInvito,
                                        llamada1 = llamada1,
                                        llamada2 = llamada2,
                                        llamada3 = llamada3,
                                        visita = visita,
                                        nombreLiderVisito = nombreLiderVisito,
                                        fechaConversion = fechaConversion,
                                        universidadVida = universidadVida,
                                        universidadVidaInicio = universidadVidaInicio,
                                        universidadVidaFin = universidadVidaFin,
                                        bautismo = bautismo,
                                        fechaBautismo = fechaBautismo,
                                        bautismoOtraCongregacion = bautismoOtraCongregacion,
                                        nombreCongregacion = nombreCongregacion,
                                        capacitacionDestino = capacitacionDestino,
                                        capacitacionDestinoInicio = capacitacionDestinoInicio,
                                        capacitacionDestinoFin = capacitacionDestinoFin,
                                        capacitacionDestinoNivel1 = capacitacionDestinoNivel1,
                                        capacitacionDestinoNivel2 = capacitacionDestinoNivel2,
                                        capacitacionDestinoNivel3 = capacitacionDestinoNivel3,
                                        celula = celula,
                                        nombreCelula = nombreCelula,
                                        rolCelula = if (celula) rolCelula else "",
                                        territorioCelula = if (celula) territorioCelula else "",
                                        fechaIngresoCelula = if (celula) fechaIngresoCelula else "",
                                        estadoCivil = estadoCivil,
                                        liderAsignado = liderAsignado,
                                        redAsignada = redAsignada
                                    )
                                    dismissWithKeyboard()
                                }
                            },
                        colors = ButtonDefaults.buttonColors(containerColor = currentAccent.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Registrar", color = Color.White)
                    }
                }
            }
        }
    }
}

// ==========================================
// CUSTOM GRID CALENDAR PICKER IMPLEMENTATION
// ==========================================
@Composable
fun CustomInlineCalendarPicker(
    currentAccent: AccentTheme,
    initialDateMs: Long,
    onDateSelected: (Long) -> Unit
) {
    var calendar by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialDateMs })
    }
    
    val yearState = calendar.get(Calendar.YEAR)
    val monthState = calendar.get(Calendar.MONTH) // 0-indexed

    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    // Calculate details
    val daysInMonth = when (monthState + 1) {
        1 -> 31
        2 -> if ((yearState % 4 == 0 && yearState % 100 != 0) || (yearState % 400 == 0)) 29 else 28
        3 -> 31
        4 -> 30
        5 -> 31
        6 -> 30
        7 -> 31
        8 -> 31
        9 -> 30
        10 -> 31
        11 -> 30
        12 -> 31
        else -> 30
    }

    // Week Starting Day
    val startDayOfWeek = remember(monthState, yearState) {
        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.YEAR, yearState)
        tempCal.set(Calendar.MONTH, monthState)
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        tempCal.get(Calendar.DAY_OF_WEEK) // 1: Sunday, 2: Monday...
    }

    val daysList = remember(daysInMonth, startDayOfWeek) {
        val list = mutableListOf<Int?>()
        val emptyDaysCount = startDayOfWeek - 1
        repeat(emptyDaysCount) { list.add(null) }
        for (i in 1..daysInMonth) {
            list.add(i)
        }
        list
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCardDark)
            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = Calendar.getInstance().apply {
                        timeInMillis = calendar.timeInMillis
                        add(Calendar.MONTH, -1)
                    }
                    calendar = newCal
                }) {
                    Text("<", color = currentAccent.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Text(
                    text = "${monthNames[monthState]} $yearState",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = {
                    val newCal = Calendar.getInstance().apply {
                        timeInMillis = calendar.timeInMillis
                        add(Calendar.MONTH, 1)
                    }
                    calendar = newCal
                }) {
                    Text(">", color = currentAccent.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val headers = listOf("Dom", "Lun", "Mar", "Mie", "Jue", "Vie", "Sab")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                headers.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubtitleDark,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val rowsCount = (daysList.size + 6) / 7
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(rowsCount) { rowIndex ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        repeat(7) { colIndex ->
                            val listIdx = rowIndex * 7 + colIndex
                            val dayNum = daysList.getOrNull(listIdx)
                            
                            if (dayNum != null) {
                                val isSelected = calendar.apply {
                                    set(Calendar.DAY_OF_MONTH, dayNum)
                                }.timeInMillis.let {
                                    val cal1 = Calendar.getInstance().apply { timeInMillis = it }
                                    val cal2 = Calendar.getInstance().apply { timeInMillis = initialDateMs }
                                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) currentAccent.primary else Color.Transparent)
                                        .clickable {
                                            val finalCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, yearState)
                                                set(Calendar.MONTH, monthState)
                                                set(Calendar.DAY_OF_MONTH, dayNum)
                                            }
                                            onDateSelected(finalCal.timeInMillis)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else OnBgDark
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLE HELPERS FOR PROGRESS REGISTRATION
// ==========================================
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    currentAccent: AccentTheme,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    testTag: String = ""
) {
    val displayLabel = if (required) "$label *" else label
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(displayLabel, fontSize = 11.sp) },
        placeholder = { Text(placeholder, color = SubtitleDark, fontSize = 12.sp) },
        leadingIcon = leadingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = currentAccent.primary,
            unfocusedBorderColor = BorderDark,
            focusedContainerColor = BgCardDark,
            unfocusedContainerColor = BgCardDark,
            focusedLabelColor = currentAccent.primary,
            unfocusedLabelColor = SubtitleDark
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth().testTag(testTag),
        singleLine = true
    )
}

@Composable
fun FormDateField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    currentAccent: AccentTheme,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = if (value == "mm/dd/aaaa" || value == "") "" else value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        placeholder = { Text(placeholder, color = SubtitleDark, fontSize = 12.sp) },
        trailingIcon = {
            IconButton(onClick = {
                keyboardController?.hide()
                showDatePicker = true
            }) {
                Icon(Icons.Default.DateRange, contentDescription = "Elegir fecha", tint = currentAccent.primary)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = currentAccent.primary,
            unfocusedBorderColor = BorderDark,
            focusedContainerColor = BgCardDark,
            unfocusedContainerColor = BgCardDark,
            focusedLabelColor = currentAccent.primary,
            unfocusedLabelColor = SubtitleDark
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
    
    if (showDatePicker) {
        Dialog(onDismissRequest = { showDatePicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgDark),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seleccionar Fecha", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomInlineCalendarPicker(
                        currentAccent = currentAccent,
                        initialDateMs = System.currentTimeMillis(),
                        onDateSelected = { ms ->
                            val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            onValueChange(formatter.format(Date(ms)))
                            showDatePicker = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FormCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    currentAccent: AccentTheme,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCardDark)
            .border(1.dp, if (checked) currentAccent.primary.copy(alpha = 0.5f) else BorderDark, RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(label, color = OnBgDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = currentAccent.primary,
                uncheckedColor = SubtitleDark
            )
        )
    }
}

fun calculateAge(birthdayString: String?): Int? {
    if (birthdayString.isNullOrBlank() || birthdayString == "mm/dd/aaaa" || birthdayString == "mm/dd/yyyy") {
        return null
    }
    return try {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val birthDate = sdf.parse(birthdayString) ?: return null
        val birthCalendar = Calendar.getInstance().apply { time = birthDate }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        age
    } catch (e: Exception) {
        null
    }
}

@Composable
fun FormChoiceChips(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    currentAccent: AccentTheme
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            color = SubtitleDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) currentAccent.primary.copy(alpha = 0.25f) else BgCardDark)
                        .border(1.dp, if (isSelected) currentAccent.primary else BorderDark, RoundedCornerShape(10.dp))
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isSelected) Color.White else SubtitleDark,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
