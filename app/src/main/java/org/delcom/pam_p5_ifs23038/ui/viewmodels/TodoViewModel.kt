package org.delcom.pam_p5_ifs23038.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23038.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23038.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23038.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23038.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23038.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23038.network.todos.service.ITodoRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface TodosUIState {
    data class Success(val data: List<ResponseTodoData>) : TodosUIState
    data class Error(val message: String) : TodosUIState
    object Loading : TodosUIState
}

sealed interface TodoUIState {
    data class Success(val data: ResponseTodoData) : TodoUIState
    data class Error(val message: String) : TodoUIState
    object Loading : TodoUIState
}

sealed interface TodoActionUIState {
    data class Success(val message: String) : TodoActionUIState
    data class Error(val message: String) : TodoActionUIState
    object Loading : TodoActionUIState
}

data class UIStateTodo(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val todos: TodosUIState = TodosUIState.Loading,
    var todo: TodoUIState = TodoUIState.Loading,
    var todoAdd: TodoActionUIState = TodoActionUIState.Loading,
    var todoChange: TodoActionUIState = TodoActionUIState.Loading,
    var todoDelete: TodoActionUIState = TodoActionUIState.Loading,
    var todoChangeCover: TodoActionUIState = TodoActionUIState.Loading,
    var profileChange: TodoActionUIState = TodoActionUIState.Loading,
    var profileChangePassword: TodoActionUIState = TodoActionUIState.Loading,
    var profileChangePhoto: TodoActionUIState = TodoActionUIState.Loading
)

@HiltViewModel
@Keep
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateTodo())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLastPage = false
    private var currentTodosList = mutableListOf<ResponseTodoData>()

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.getUserMe(authToken) }
                    .fold(
                        onSuccess = { if (it.status == "success") ProfileUIState.Success(it.data!!.user) else ProfileUIState.Error(it.message) },
                        onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(profile = tmpState)
            }
        }
    }

    // UPDATE: Membutuhkan name dan username
    fun putUserMe(authToken: String, name: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChange = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.putUserMe(authToken, RequestUserChange(name = name, username = username)) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                        onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(profileChange = tmpState)
            }
        }
    }

    // UPDATE: Membutuhkan password dan newPassword
    fun putUserMePassword(authToken: String, password: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChangePassword = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.putUserMePassword(authToken, RequestUserChangePassword(newPassword = newPassword, password = password)) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                        onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(profileChangePassword = tmpState)
            }
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChangePhoto = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.putUserMePhoto(authToken, file) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                        onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(profileChangePhoto = tmpState)
            }
        }
    }

    // ... (Fungsi Todo lainnya biarkan sama seperti yang sudah saya berikan sebelumnya) ...

    fun getAllTodos(
        authToken: String,
        search: String? = null,
        isDone: Boolean? = null,
        isLoadMore: Boolean = false
    ) {
        if (!isLoadMore) {
            currentPage = 1
            isLastPage = false
            currentTodosList.clear()
            _uiState.update { it.copy(todos = TodosUIState.Loading) }
        }

        if (isLastPage) return

        viewModelScope.launch {
            _uiState.update { state ->
                val tmpState = runCatching {
                    repository.getTodos(authToken, search, isDone, currentPage, 10)
                }.fold(
                    onSuccess = { response ->
                        if (response.status == "success") {
                            val newTodos = response.data!!.todos
                            if (newTodos.size < 10) {
                                isLastPage = true
                            }
                            currentTodosList.addAll(newTodos)
                            currentPage++
                            TodosUIState.Success(currentTodosList.toList())
                        } else {
                            TodosUIState.Error(response.message)
                        }
                    },
                    onFailure = { error ->
                        TodosUIState.Error(error.message ?: "Unknown error")
                    }
                )
                state.copy(todos = tmpState)
            }
        }
    }

    fun getAllTodos(authToken: String, search: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(todos = TodosUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.getTodos(authToken, search) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodosUIState.Success(it.data!!.todos) else TodosUIState.Error(it.message) },
                        onFailure = { TodosUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(todos = tmpState)
            }
        }
    }

    fun postTodo(authToken: String, title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoAdd = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.postTodo(authToken, RequestTodo(title = title, description = description)) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                        onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(todoAdd = tmpState)
            }
        }
    }

    fun getTodoById(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todo = TodoUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.getTodoById(authToken, todoId) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoUIState.Success(it.data!!.todo) else TodoUIState.Error(it.message) },
                        onFailure = { TodoUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(todo = tmpState)
            }
        }
    }

    fun putTodo(authToken: String, todoId: String, title: String, description: String, isDone: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChange = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.putTodo(authToken, todoId, RequestTodo(title = title, description = description, isDone = isDone)) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                        onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(todoChange = tmpState)
            }
        }
    }

    fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChangeCover = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.putTodoCover(authToken, todoId, file) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                        onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(todoChangeCover = tmpState)
            }
        }
    }

    fun deleteTodo(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoDelete = TodoActionUIState.Loading) }
            _uiState.update { state ->
                val tmpState = runCatching { repository.deleteTodo(authToken, todoId) }
                    .fold(
                        onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                        onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
                    )
                state.copy(todoDelete = tmpState)
            }
        }
    }
}