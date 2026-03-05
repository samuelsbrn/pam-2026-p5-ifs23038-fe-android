package org.delcom.pam_p5_ifs23038.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23038.R
import org.delcom.pam_p5_ifs23038.helper.ConstHelper
import org.delcom.pam_p5_ifs23038.helper.RouteHelper
import org.delcom.pam_p5_ifs23038.helper.ToolsHelper
import org.delcom.pam_p5_ifs23038.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23038.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23038.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23038.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23038.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23038.ui.theme.DelcomTheme
import org.delcom.pam_p5_ifs23038.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23038.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23038.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23038.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23038.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23038.ui.viewmodels.TodoViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ResponseUserData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        if (uiStateTodo.profile is ProfileUIState.Success) {
            profile = (uiStateTodo.profile as ProfileUIState.Success).data
            isLoading = false
            return@LaunchedEffect
        }
        todoViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateTodo.profile) {
        if (uiStateTodo.profile !is ProfileUIState.Loading) {
            isLoading = false
            if (uiStateTodo.profile is ProfileUIState.Success) {
                profile = (uiStateTodo.profile as ProfileUIState.Success).data
            } else {
                RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            }
        }
    }

    LaunchedEffect(uiStateTodo.profileChange) {
        if (uiStateTodo.profileChange is TodoActionUIState.Success) {
            Toast.makeText(context, "Profil berhasil diubah", Toast.LENGTH_SHORT).show()
            todoViewModel.getProfile(authToken ?: "")
            uiStateTodo.profileChange = TodoActionUIState.Loading // Reset state manual
        } else if (uiStateTodo.profileChange is TodoActionUIState.Error) {
            Toast.makeText(context, (uiStateTodo.profileChange as TodoActionUIState.Error).message, Toast.LENGTH_SHORT).show()
            uiStateTodo.profileChange = TodoActionUIState.Loading
        }
    }

    LaunchedEffect(uiStateTodo.profileChangePassword) {
        if (uiStateTodo.profileChangePassword is TodoActionUIState.Success) {
            Toast.makeText(context, "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show()
            uiStateTodo.profileChangePassword = TodoActionUIState.Loading
        } else if (uiStateTodo.profileChangePassword is TodoActionUIState.Error) {
            Toast.makeText(context, (uiStateTodo.profileChangePassword as TodoActionUIState.Error).message, Toast.LENGTH_SHORT).show()
            uiStateTodo.profileChangePassword = TodoActionUIState.Loading
        }
    }

    LaunchedEffect(uiStateTodo.profileChangePhoto) {
        if (uiStateTodo.profileChangePhoto is TodoActionUIState.Success) {
            Toast.makeText(context, "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show()
            todoViewModel.getProfile(authToken ?: "")
            uiStateTodo.profileChangePhoto = TodoActionUIState.Loading
        } else if (uiStateTodo.profileChangePhoto is TodoActionUIState.Error) {
            Toast.makeText(context, (uiStateTodo.profileChangePhoto as TodoActionUIState.Error).message, Toast.LENGTH_SHORT).show()
            uiStateTodo.profileChangePhoto = TodoActionUIState.Loading
        }
    }

    fun onLogout(token: String) {
        isLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isLoading || profile == null) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(
            text = "Profile",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
        TopAppBarMenuItem(
            text = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            route = null,
            onClick = { onLogout(authToken ?: "") }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Profile",
            showBackButton = false,
            customMenuItems = menuItems
        )

        Box(modifier = Modifier.weight(1f)) {
            ProfileUI(
                profile = profile!!,
                onChangePhoto = { file ->
                    val multipart = ToolsHelper.uriToMultipart(context, file, "file")
                    todoViewModel.putUserMePhoto(authToken ?: "", multipart)
                },
                onEditProfile = { name, username ->
                    todoViewModel.putUserMe(authToken ?: "", name, username)
                },
                onChangePassword = { oldPass, newPass ->
                    todoViewModel.putUserMePassword(authToken ?: "", oldPass, newPass)
                }
            )
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentUsername: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var username by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Profil") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, username) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Kata Sandi") },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kata Sandi Saat Ini") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Kata Sandi Baru") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password, newPassword) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ProfileUI(
    profile: ResponseUserData,
    onChangePhoto: (Uri) -> Unit,
    onEditProfile: (String, String) -> Unit,
    onChangePassword: (String, String) -> Unit
) {
    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }

    // State untuk menampung gambar sementara sebelum disimpan (mirip seperti cover todo)
    var dataFile by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        dataFile = uri
    }

    if (showEditProfile) {
        EditProfileDialog(
            currentName = profile.name,
            currentUsername = profile.username,
            onDismiss = { showEditProfile = false },
            onConfirm = { name, username ->
                showEditProfile = false
                onEditProfile(name, username)
            }
        )
    }

    if (showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { showChangePassword = false },
            onConfirm = { oldPass, newPass ->
                showChangePassword = false
                onChangePassword(oldPass, newPass)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- BAGIAN FOTO PROFIL ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bingkai Gambar
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .clickable {
                        imagePicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    // Kita operasikan profile.updatedAt untuk menghapus cache di Coil otomatis ketika data update
                    model = dataFile ?: ToolsHelper.getUserImage(profile.id, profile.updatedAt),
                    contentDescription = "Photo Profil",
                    placeholder = painterResource(R.drawable.img_placeholder),
                    error = painterResource(R.drawable.img_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Teks Status Gambar
            Text(
                text = if (dataFile == null) "Sentuh foto untuk mengubah" else "Foto baru dipilih",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Munculkan tombol Simpan hanya jika gambar baru dipilih (Mirip TodosDetailScreen)
            if (dataFile != null) {
                Button(
                    onClick = {
                        onChangePhoto(dataFile!!)
                        dataFile = null // Hilangkan tombol simpan setelah diklik
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(0.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Simpan Foto",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = "@${profile.username}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TOMBOL AKSI ---
        Button(
            onClick = { showEditProfile = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Ubah Informasi Profil")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showChangePassword = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Ubah Kata Sandi")
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewProfileUI() {
    DelcomTheme {
        ProfileUI(
            profile = ResponseUserData(id = "", name = "Samuel Sibarani", username = "samuelsbrn", createdAt = "", updatedAt = ""),
            onChangePhoto = {},
            onEditProfile = { _, _ -> },
            onChangePassword = { _, _ -> }
        )
    }
}