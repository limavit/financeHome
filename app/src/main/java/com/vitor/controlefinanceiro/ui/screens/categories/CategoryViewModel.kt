package com.vitor.controlefinanceiro.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitor.controlefinanceiro.data.local.entity.CategoryEntity
import com.vitor.controlefinanceiro.data.repository.CategoryRepository
import com.vitor.controlefinanceiro.domain.model.CategoryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryFormState(
    val id: String? = null,
    val name: String = "",
    val type: CategoryType = CategoryType.AMBOS
)

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {
    val categories = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val message = MutableStateFlow<String?>(null)

    fun save(form: CategoryFormState) = viewModelScope.launch {
        runCatching {
            repository.save(form.name, form.type, id = form.id ?: java.util.UUID.randomUUID().toString())
            message.value = if (form.id != null) "Categoria atualizada." else "Categoria salva."
        }.onFailure { message.value = it.message ?: "Nao foi possivel salvar." }
    }

    fun toFormState(category: CategoryEntity): CategoryFormState = CategoryFormState(
        id = category.id,
        name = category.name,
        type = category.type
    )

    fun setActive(category: CategoryEntity, active: Boolean) = viewModelScope.launch {
        repository.setActive(category, active)
    }
    fun consumeMessage() { message.value = null }
}
