package com.calica.stupidble.ui.characteristicedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calica.stupidble.data.database.CharacteristicDataType
import com.calica.stupidble.data.database.Endianness
import com.calica.stupidble.data.repository.MetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacteristicEditViewModel(
    private val metadataRepository: MetadataRepository
) : ViewModel() {

    private val _dataType = MutableStateFlow(CharacteristicDataType.HEX_RAW)
    val dataType: StateFlow<CharacteristicDataType> = _dataType.asStateFlow()

    private val _endianness = MutableStateFlow(Endianness.LITTLE_ENDIAN)
    val endianness: StateFlow<Endianness> = _endianness.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun loadMetadata(deviceAddress: String, serviceUuid: String, characteristicUuid: String) {
        viewModelScope.launch {
            val metadata = metadataRepository.getMetadataSync(deviceAddress, serviceUuid, characteristicUuid)
            if (metadata != null) {
                _dataType.value = metadata.dataType
                _endianness.value = metadata.endianness
            }
        }
    }

    fun setDataType(type: CharacteristicDataType) {
        _dataType.value = type
    }

    fun setEndianness(endianness: Endianness) {
        _endianness.value = endianness
    }

    suspend fun saveMetadata(deviceAddress: String, serviceUuid: String, characteristicUuid: String) {
        _isSaving.value = true
        _saveSuccess.value = false

        try {
            metadataRepository.saveMetadata(
                deviceAddress = deviceAddress,
                serviceUuid = serviceUuid,
                characteristicUuid = characteristicUuid,
                dataType = _dataType.value,
                endianness = _endianness.value
            )
            _saveSuccess.value = true
        } catch (e: Exception) {
            // Handle error if needed
            e.printStackTrace()
        } finally {
            _isSaving.value = false
        }
    }
}

class CharacteristicEditViewModelFactory(
    private val metadataRepository: MetadataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacteristicEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharacteristicEditViewModel(metadataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

