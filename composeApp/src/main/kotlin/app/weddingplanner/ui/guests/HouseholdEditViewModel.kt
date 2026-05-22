package app.weddingplanner.ui.guests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.domain.GuestInput
import app.weddingplanner.domain.HouseholdInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MemberDraft(
    val key: Int,
    val name: String = "",
    val isMainContact: Boolean = false,
    val diet: String = "",
    val notes: String = "",
)

data class HouseholdEditUiState(
    val isLoading: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val tagsText: String = "",
    val notes: String = "",
    val members: List<MemberDraft> = listOf(MemberDraft(key = 0, isMainContact = true)),
    val errorMessage: String? = null,
    val saved: Boolean = false,
    val saving: Boolean = false,
) {
    val canSave: Boolean
        get() = displayName.isNotBlank() &&
            members.isNotEmpty() &&
            members.all { it.name.isNotBlank() } &&
            members.count { it.isMainContact } == 1
}

class HouseholdEditViewModel(
    private val apiClient: ApiClient,
    private val householdId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HouseholdEditUiState())
    val uiState: StateFlow<HouseholdEditUiState> = _uiState.asStateFlow()
    private var nextKey = 1

    init {
        if (householdId != null) loadExisting(householdId)
    }

    private fun loadExisting(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            apiClient.getHousehold(id)
                .onSuccess { hh ->
                    nextKey = hh.members.size
                    _uiState.value = HouseholdEditUiState(
                        displayName = hh.displayName,
                        email = hh.email.orEmpty(),
                        phone = hh.phone.orEmpty(),
                        tagsText = hh.tags.joinToString(", "),
                        notes = hh.notes.orEmpty(),
                        members = hh.members.mapIndexed { i, g ->
                            MemberDraft(
                                key = i,
                                name = g.name,
                                isMainContact = g.isMainContact,
                                diet = g.diet.orEmpty(),
                                notes = g.notes.orEmpty(),
                            )
                        },
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Kunde inte hämta hushållet",
                    )
                }
        }
    }

    fun update(block: HouseholdEditUiState.() -> HouseholdEditUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun updateMember(key: Int, block: MemberDraft.() -> MemberDraft) {
        _uiState.value = _uiState.value.copy(
            members = _uiState.value.members.map { if (it.key == key) it.block() else it },
        )
    }

    fun setMainContact(key: Int) {
        _uiState.value = _uiState.value.copy(
            members = _uiState.value.members.map { it.copy(isMainContact = it.key == key) },
        )
    }

    fun addMember() {
        val needsMain = _uiState.value.members.none { it.isMainContact }
        _uiState.value = _uiState.value.copy(
            members = _uiState.value.members + MemberDraft(key = nextKey++, isMainContact = needsMain),
        )
    }

    fun removeMember(key: Int) {
        val remaining = _uiState.value.members.filterNot { it.key == key }
        val fixed = if (remaining.isNotEmpty() && remaining.none { it.isMainContact }) {
            remaining.mapIndexed { i, m -> if (i == 0) m.copy(isMainContact = true) else m }
        } else remaining
        _uiState.value = _uiState.value.copy(members = fixed)
    }

    fun save() {
        val current = _uiState.value
        if (!current.canSave) return
        val input = HouseholdInput(
            displayName = current.displayName.trim(),
            email = current.email.trim().ifBlank { null },
            phone = current.phone.trim().ifBlank { null },
            tags = current.tagsText
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() },
            notes = current.notes.trim().ifBlank { null },
            members = current.members.map {
                GuestInput(
                    name = it.name.trim(),
                    isMainContact = it.isMainContact,
                    diet = it.diet.trim().ifBlank { null },
                    notes = it.notes.trim().ifBlank { null },
                )
            },
        )
        viewModelScope.launch {
            _uiState.value = current.copy(saving = true, errorMessage = null)
            val result = if (householdId == null) {
                apiClient.createHousehold(input)
            } else {
                apiClient.updateHousehold(householdId, input)
            }
            result
                .onSuccess { _uiState.value = _uiState.value.copy(saving = false, saved = true) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        saving = false,
                        errorMessage = it.message ?: "Kunde inte spara",
                    )
                }
        }
    }
}
