package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.RegistroEntity
import com.example.data.RegistroRepository
import com.example.data.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RegistroRepository
    private val prefs = application.getSharedPreferences("registro_prefs", Context.MODE_PRIVATE)

    // User authentication state
    val loggedInUserEmail = MutableStateFlow<String?>(prefs.getString("logged_in_user_email", null))
    val loggedInUserName = MutableStateFlow<String>(prefs.getString("logged_in_user_name", "") ?: "")

    // Room Database Flows
    private val _allRegistros = MutableStateFlow<List<RegistroEntity>>(emptyList())
    val allRegistros: StateFlow<List<RegistroEntity>> = _allRegistros.asStateFlow()
    
    // User Input States
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("Todos")

    // UI States
    val userName = MutableStateFlow(prefs.getString("user_name", "Orangelis") ?: "Orangelis")
    val userEmail = MutableStateFlow(prefs.getString("user_email", "orangelisvalor@gmail.com") ?: "orangelisvalor@gmail.com")
    val accentColorIndex = MutableStateFlow(prefs.getInt("accent_color_index", 0))
    val isLightMode = MutableStateFlow(prefs.getBoolean("is_light_mode", false))

    fun toggleLightMode(isLight: Boolean) {
        isLightMode.value = isLight
        prefs.edit().putBoolean("is_light_mode", isLight).apply()
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RegistroRepository(database.registroDao(), database.userDao())

        // Observe Room changes for the logged-in user dynamically
        viewModelScope.launch {
            loggedInUserEmail.collect { email ->
                if (email != null) {
                    repository.getRegistrosByUser(email).collect { list ->
                        _allRegistros.value = list
                    }
                } else {
                    _allRegistros.value = emptyList()
                }
            }
        }
    }

    val selectedLeaderFilter = MutableStateFlow("Todos")
    val selectedNetworkFilter = MutableStateFlow("Todos")

    // Lists of unique leaders and networks present in current members list
    val uniqueLeaders: StateFlow<List<String>> = _allRegistros
        .map { list -> list.map { it.liderAsignado }.filter { it.isNotBlank() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueNetworks: StateFlow<List<String>> = _allRegistros
        .map { list -> list.map { it.redAsignada }.filter { it.isNotBlank() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Flow based on search query AND category selection AND leader filter AND network filter
    val filteredRegistros: StateFlow<List<RegistroEntity>> = combine(
        _allRegistros,
        searchQuery,
        selectedCategoryFilter,
        selectedLeaderFilter,
        selectedNetworkFilter
    ) { list, query, category, leader, network ->
        list.filter { item ->
            val matchesQuery = query.isBlank() || 
                    item.nombre.contains(query, ignoreCase = true) || 
                    item.apellido.contains(query, ignoreCase = true) ||
                    item.quienInvito.contains(query, ignoreCase = true) ||
                    item.nombreCelula.contains(query, ignoreCase = true) ||
                    item.liderAsignado.contains(query, ignoreCase = true) ||
                    item.redAsignada.contains(query, ignoreCase = true) ||
                    item.territorioCelula.contains(query, ignoreCase = true) ||
                    item.rolCelula.contains(query, ignoreCase = true)
            val matchesCategory = category == "Todos" || item.category == category
            val matchesLeader = leader == "Todos" || item.liderAsignado == leader
            val matchesNetwork = network == "Todos" || item.redAsignada == network
            matchesQuery && matchesCategory && matchesLeader && matchesNetwork
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics Flows
    val totalCount: StateFlow<Int> = _allRegistros
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categoryDistribution: StateFlow<Map<String, Int>> = _allRegistros
        .map { list ->
            list.groupBy { it.category }.mapValues { it.value.size }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Authentication operations
    fun login(emailText: String, passwordText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val formattedEmail = emailText.trim().lowercase()
        if (emailText.isBlank() || passwordText.isBlank()) {
            onError("Todos los campos de inicio de sesión son obligatorios")
            return
        }
        viewModelScope.launch {
            val user = repository.getUserByEmail(formattedEmail)
            if (user != null && user.passwordHash == passwordText) {
                // Success
                prefs.edit().apply {
                    putString("logged_in_user_email", formattedEmail)
                    putString("logged_in_user_name", user.name)
                    apply()
                }
                loggedInUserEmail.value = formattedEmail
                loggedInUserName.value = user.name
                userName.value = user.name
                userEmail.value = formattedEmail
                onSuccess()
            } else {
                onError("Correo o contraseña incorrectos")
            }
        }
    }

    fun signUp(nameText: String, emailText: String, passwordText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val formattedEmail = emailText.trim().lowercase()
        if (nameText.isBlank() || emailText.isBlank() || passwordText.isBlank()) {
            onError("Todos los campos del registro son obligatorios")
            return
        }
        viewModelScope.launch {
            val existing = repository.getUserByEmail(formattedEmail)
            if (existing != null) {
                onError("El correo electrónico ya está registrado")
            } else {
                val newUser = UserEntity(
                    email = formattedEmail,
                    passwordHash = passwordText,
                    name = nameText.trim()
                )
                repository.insertUser(newUser)
                
                // Automatically log inside after registration
                prefs.edit().apply {
                    putString("logged_in_user_email", formattedEmail)
                    putString("logged_in_user_name", newUser.name)
                    apply()
                }
                loggedInUserEmail.value = formattedEmail
                loggedInUserName.value = newUser.name
                userName.value = newUser.name
                userEmail.value = formattedEmail
                onSuccess()
            }
        }
    }

    fun loginWithGoogle(email: String, displayName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val formattedEmail = email.trim().lowercase()
        if (formattedEmail.isBlank()) {
            onError("Dirección de correo de Google no válida")
            return
        }
        viewModelScope.launch {
            val user = repository.getUserByEmail(formattedEmail)
            if (user != null) {
                // El usuario ya existe, iniciar sesión
                prefs.edit().apply {
                    putString("logged_in_user_email", formattedEmail)
                    putString("logged_in_user_name", user.name)
                    apply()
                }
                loggedInUserEmail.value = formattedEmail
                loggedInUserName.value = user.name
                userName.value = user.name
                userEmail.value = formattedEmail
                onSuccess()
            } else {
                // No existe el usuario, crearlo automáticamente
                val newUser = UserEntity(
                    email = formattedEmail,
                    passwordHash = "google_auth_placeholder_pwd",
                    name = if (displayName.isNotBlank()) displayName else "Usuario Google"
                )
                repository.insertUser(newUser)
                
                prefs.edit().apply {
                    putString("logged_in_user_email", formattedEmail)
                    putString("logged_in_user_name", newUser.name)
                    apply()
                }
                loggedInUserEmail.value = formattedEmail
                loggedInUserName.value = newUser.name
                userName.value = newUser.name
                userEmail.value = formattedEmail
                onSuccess()
            }
        }
    }

    fun logout() {
        prefs.edit().apply {
            remove("logged_in_user_email")
            remove("logged_in_user_name")
            apply()
        }
        loggedInUserEmail.value = null
        loggedInUserName.value = ""
        _allRegistros.value = emptyList()
    }

    // User Operations
    fun addRegistro(
        nombre: String,
        apellido: String,
        telefono: String,
        cedula: String,
        cumpleanos: String,
        direccionCompleta: String,
        quienInvito: String,
        llamada1: Boolean,
        llamada2: Boolean,
        llamada3: Boolean,
        visita: Boolean,
        nombreLiderVisito: String,
        fechaConversion: String,
        universidadVida: Boolean,
        universidadVidaInicio: String,
        universidadVidaFin: String,
        bautismo: Boolean,
        fechaBautismo: String,
        bautismoOtraCongregacion: Boolean,
        nombreCongregacion: String,
        capacitacionDestino: Boolean,
        capacitacionDestinoInicio: String,
        capacitacionDestinoFin: String,
        capacitacionDestinoNivel1: Boolean,
        capacitacionDestinoNivel2: Boolean,
        capacitacionDestinoNivel3: Boolean,
        celula: Boolean,
        nombreCelula: String,
        rolCelula: String = "",
        territorioCelula: String = "",
        fechaIngresoCelula: String = "",
        estadoCivil: String = "",
        liderAsignado: String = "",
        redAsignada: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        val email = loggedInUserEmail.value ?: ""
        
        // Compute compatibility fields:
        val computedTitle = "$nombre $apellido"
        
        // Category matches the highest step completed:
        val computedCategory = when {
            celula -> "Célula"
            capacitacionDestino -> "Capacitación"
            bautismo -> "Bautismo"
            universidadVida -> "Univ. Vida"
            visita || llamada1 || llamada2 || llamada3 -> "Seguimiento"
            else -> "Otros"
        }
        
        val computedContent = "Tel: $telefono" + (if (cedula.isNotBlank()) " | ID: $cedula" else "") + (if (direccionCompleta.isNotBlank()) " | Dir: $direccionCompleta" else "")
        val ratingStars = when {
            celula -> 5
            capacitacionDestino -> 4
            bautismo -> 3
            universidadVida -> 2
            visita -> 1
            else -> 0
        }

        viewModelScope.launch {
            repository.insert(
                RegistroEntity(
                    userEmail = email,
                    title = computedTitle,
                    category = computedCategory,
                    content = computedContent,
                    timestamp = timestamp,
                    rating = ratingStars,
                    notes = if (quienInvito.isNotBlank()) "Invitado por: $quienInvito" else "",
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
                    rolCelula = rolCelula,
                    territorioCelula = territorioCelula,
                    fechaIngresoCelula = fechaIngresoCelula,
                    estadoCivil = estadoCivil,
                    liderAsignado = liderAsignado,
                    redAsignada = redAsignada
                )
            )
        }
    }

    fun updateRegistro(entity: RegistroEntity) {
        viewModelScope.launch {
            repository.update(entity)
        }
    }

    fun deleteRegistro(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    // Profile Settings Operations
    fun updateProfile(name: String, email: String, colorIndex: Int) {
        userName.value = name
        userEmail.value = email
        accentColorIndex.value = colorIndex

        prefs.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putInt("accent_color_index", colorIndex)
            apply()
        }
    }
}
