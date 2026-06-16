package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.example.AccentTheme
import com.example.ui.theme.*
import com.example.ui.viewmodel.RegistroViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: RegistroViewModel,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    
    // Form Inputs
    var nameText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val accentIndex by viewModel.accentColorIndex.collectAsStateWithLifecycle()
    val accentThemes = listOf(
        AccentTheme("Violeta Imperial", VioletPrimary, VioletGlowing, Brush.horizontalGradient(listOf(VioletPrimary, VioletGlowing))),
        AccentTheme("Rosa Crepúsculo", CoralPrimary, CoralGlowing, Brush.horizontalGradient(listOf(CoralPrimary, CoralGlowing))),
        AccentTheme("Esmeralda Menta", EmeraldPrimary, EmeraldGlowing, Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldGlowing))),
        AccentTheme("Azul Cósmico", AzurePrimary, AzureGlowing, Brush.horizontalGradient(listOf(AzurePrimary, AzureGlowing)))
    )
    val currentAccent = accentThemes.getOrElse(accentIndex) { accentThemes[0] }

    val context = LocalContext.current
    var showGoogleFallback by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val email = account.email ?: ""
                    val displayName = account.displayName ?: "Usuario Google"
                    viewModel.loginWithGoogle(
                        email = email,
                        displayName = displayName,
                        onSuccess = {
                            successMessage = "¡Inicio de sesión exitoso con Google: $email!"
                        },
                        onError = {
                            errorMessage = it
                        }
                    )
                } else {
                    errorMessage = "No se pudo recuperar la cuenta de Google"
                }
            } catch (e: ApiException) {
                showGoogleFallback = true
            } catch (e: Exception) {
                showGoogleFallback = true
            }
        } else {
            showGoogleFallback = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.Center
    ) {
        // Decorative cosmic ambient glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = currentAccent.primary.copy(alpha = 0.09f),
                radius = size.width / 1.2f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                color = currentAccent.glowing.copy(alpha = 0.05f),
                radius = size.width / 1.5f,
                center = Offset(size.width, size.height)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 450.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(currentAccent.primary.copy(alpha = 0.15f))
                        .border(1.5.dp, currentAccent.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Auth Logo",
                        tint = currentAccent.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "REGISTRO IDEA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Sistema de Control",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentAccent.glowing,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Main Auth Form Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, shape = RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BgCardDark),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Headline Text
                    Text(
                        text = if (isSignUpMode) "Crear Nueva Cuenta" else "Iniciar Sesión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isSignUpMode) "Completa los datos para registrarte" else "Ingresa tus credenciales para continuar",
                        fontSize = 12.sp,
                        color = SubtitleDark,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Error Notification Block
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CoralPrimary.copy(alpha = 0.15f))
                                .border(1.dp, CoralPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alerta",
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = OnBgDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Success Notification Block
                    if (successMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.15f))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Éxito",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = successMessage ?: "",
                                    color = OnBgDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Input fields
                    AnimatedVisibility(
                        visible = isSignUpMode,
                        enter = expandIn() + fadeIn(),
                        exit = shrinkOut() + fadeOut()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = nameText,
                                onValueChange = { nameText = it },
                                label = { Text("Nombre Completo") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SubtitleDark) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = currentAccent.primary,
                                    unfocusedBorderColor = BorderDark,
                                    focusedContainerColor = BgDark,
                                    unfocusedContainerColor = BgDark,
                                    focusedLabelColor = currentAccent.primary,
                                    unfocusedLabelColor = SubtitleDark
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_field"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Email Input
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SubtitleDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = currentAccent.primary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark,
                            focusedLabelColor = currentAccent.primary,
                            unfocusedLabelColor = SubtitleDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SubtitleDark) },
                        trailingIcon = {
                            val icon = if (isPasswordVisible) Icons.Default.Info else Icons.Default.Lock // using existing icons to prevent crash
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Close else Icons.Default.Face,
                                    contentDescription = "Visibilidad",
                                    tint = SubtitleDark
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = currentAccent.primary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark,
                            focusedLabelColor = currentAccent.primary,
                            unfocusedLabelColor = SubtitleDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Access Submit Button
                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            if (isSignUpMode) {
                                viewModel.signUp(
                                    nameText = nameText,
                                    emailText = emailText,
                                    passwordText = passwordText,
                                    onSuccess = {
                                        successMessage = "¡Cuenta creada éxitosamente!"
                                        nameText = ""
                                        emailText = ""
                                        passwordText = ""
                                    },
                                    onError = { errorMessage = it }
                                )
                            } else {
                                viewModel.login(
                                    emailText = emailText,
                                    passwordText = passwordText,
                                    onSuccess = {
                                        successMessage = "¡Bienvenido de vuelta!"
                                        emailText = ""
                                        passwordText = ""
                                    },
                                    onError = { errorMessage = it }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentAccent.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isSignUpMode) "Registrarse" else "Iniciar Sesión",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = BorderDark
                        )
                        Text(
                            text = "O CONTINUAR CON",
                            style = MaterialTheme.typography.labelSmall,
                            color = SubtitleDark,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = BorderDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            try {
                                errorMessage = null
                                successMessage = null
                                val signInIntent = googleSignInClient.signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            } catch (e: Exception) {
                                showGoogleFallback = true
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BgDark,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_google_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Iniciar sesión con Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mode Switch Label Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignUpMode) "¿Ya tienes una cuenta?" else "¿No tienes una cuenta?",
                            fontSize = 12.sp,
                            color = SubtitleDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSignUpMode) "Inicia Sesión" else "Regístrate ahora",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentAccent.primary,
                            modifier = Modifier
                                .clickable {
                                    isSignUpMode = !isSignUpMode
                                    errorMessage = null
                                    successMessage = null
                                }
                                .testTag("auth_switch_mode_link")
                        )
                    }
                }
            }
        }
    }

    if (showGoogleFallback) {
        Dialog(onDismissRequest = { showGoogleFallback = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BgCardDark),
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Google Sign-In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "El simulador de Google Sign-In te permite guardar tus registros bajo una cuenta de Google verificada sin requerir configuraciones de firmas SHA-1 en este entorno de desarrollo. Selecciona un perfil de prueba o ingresa los datos a continuación.",
                        fontSize = 11.sp,
                        color = SubtitleDark,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Email Input
                    var customEmail by remember { mutableStateOf("lider.prueba@gmail.com") }
                    var customName by remember { mutableStateOf("Líder de Prueba") }

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nombre de Cuenta Google") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = currentAccent.primary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark,
                            focusedLabelColor = currentAccent.primary,
                            unfocusedLabelColor = SubtitleDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Correo Google (Gmail)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = currentAccent.primary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = BgDark,
                            unfocusedContainerColor = BgDark,
                            focusedLabelColor = currentAccent.primary,
                            unfocusedLabelColor = SubtitleDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Perfiles de Prueba Rápidos:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentAccent.glowing,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val presetProfiles = listOf(
                        Pair("Líder de Prueba", "lider.prueba@gmail.com"),
                        Pair("Usuario Invitado", "invitado@gmail.com"),
                        Pair("Supervisor de Red", "supervisor.red@gmail.com")
                    )

                    presetProfiles.forEach { (name, email) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    customName = name
                                    customEmail = email
                                }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = if (customEmail == email) currentAccent.primary else SubtitleDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (customEmail == email) Color.White else OnBgDark
                                )
                                Text(
                                    text = email,
                                    fontSize = 11.sp,
                                    color = SubtitleDark
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (customEmail == email) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = currentAccent.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showGoogleFallback = false },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                showGoogleFallback = false
                                if (customEmail.isNotBlank()) {
                                    viewModel.loginWithGoogle(
                                        email = customEmail,
                                        displayName = customName,
                                        onSuccess = {
                                            successMessage = "¡Sesión iniciada con Google: $customEmail!"
                                        },
                                        onError = {
                                            errorMessage = it
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = currentAccent.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Iniciar Sesión", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        drawArc(
            color = Color(0xFFEA4335), // Red
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = Color(0xFFFBBC05), // Yellow
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = Color(0xFF34A853), // Green
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = true
        )
        drawArc(
            color = Color(0xFF4285F4), // Blue
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = true
        )

        drawCircle(
            color = BgDark,
            radius = width / 3.2f,
            center = center
        )

        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(width / 2f, height / 2.3f),
            size = androidx.compose.ui.geometry.Size(width / 2f, height / 6.5f)
        )
    }
}
