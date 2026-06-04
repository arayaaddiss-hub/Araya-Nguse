package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LulaState {
    NORMAL,
    EATING,
    PLAYING,
    SLEEPING,
    LOVED // When pet behind ears
}

class LulaViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val statsDao = db.petStatsDao()
    private val logDao = db.careLogDao()
    private val chatDao = db.chatMessageDao()

    // Current Action State (used for Lula's image animation state in the UI)
    private val _lulaState = MutableStateFlow(LulaState.NORMAL)
    val lulaState: StateFlow<LulaState> = _lulaState.asStateFlow()

    // Timer Job for resetting state back to normal
    private var stateResetJob: Job? = null

    // Room Flows
    val petStatsFlow: StateFlow<PetStats> = statsDao.getPetStatsFlow()
        .map { it ?: PetStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetStats())

    val careLogsFlow: StateFlow<List<CareLog>> = logDao.getRecentLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessagesFlow: StateFlow<List<ChatMessage>> = chatDao.getAllMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat Loading State
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    init {
        // Initialize database with default stats if null
        viewModelScope.launch {
            val stats = statsDao.getPetStats()
            if (stats == null) {
                statsDao.updatePetStats(PetStats())
                logDao.insertLog(CareLog(message = "Lula the cute Lioness Cub has entered the savanna game!", actionType = "SYSTEM"))
            }
        }
    }

    private fun setTemporaryState(state: LulaState, durationMs: Long) {
        stateResetJob?.cancel()
        _lulaState.value = state
        stateResetJob = viewModelScope.launch {
            delay(durationMs)
            _lulaState.value = LulaState.NORMAL
        }
    }

    // --- Care Actions ---

    fun feedLula() {
        viewModelScope.launch {
            val current = statsDao.getPetStats() ?: PetStats()
            
            // Stats updates: Fullness +25, energy -5, happiness +10, bond +15
            val nextFullness = (current.hunger + 25).coerceAtMost(100)
            val nextThirst = (current.thirst - 8).coerceAtLeast(0)
            val nextEnergy = (current.energy - 5).coerceAtLeast(10)
            val nextHappiness = (current.happiness + 10).coerceAtMost(100)
            
            val addedBond = 15
            val nextBond = current.bond + addedBond
            val nextLevel = calculateLevel(nextBond)

            val nextStats = current.copy(
                hunger = nextFullness,
                thirst = nextThirst,
                energy = nextEnergy,
                happiness = nextHappiness,
                bond = nextBond,
                level = nextLevel
            )
            
            statsDao.updatePetStats(nextStats)
            logDao.insertLog(CareLog(message = "You fed Lula sweet savanna berries! Nom nom nom! (+25 Fullness)", actionType = "FEED"))
            
            setTemporaryState(LulaState.EATING, 2500)
        }
    }

    fun playWithLula() {
        viewModelScope.launch {
            val current = statsDao.getPetStats() ?: PetStats()
            if (current.energy < 20) {
                logDao.insertLog(CareLog(message = "Lula is too tuckered out to play! Let her nap first. 🐾", actionType = "SYSTEM"))
                return@launch
            }

            // Stats updates: Fullness -15, thirst -15, energy -20, happiness +30, bond +20
            val nextFullness = (current.hunger - 15).coerceAtLeast(0)
            val nextThirst = (current.thirst - 15).coerceAtLeast(0)
            val nextEnergy = (current.energy - 20).coerceAtLeast(0)
            val nextHappiness = (current.happiness + 30).coerceAtMost(100)
            
            val addedBond = 20
            val nextBond = current.bond + addedBond
            val nextLevel = calculateLevel(nextBond)

            val nextStats = current.copy(
                hunger = nextFullness,
                thirst = nextThirst,
                energy = nextEnergy,
                happiness = nextHappiness,
                bond = nextBond,
                level = nextLevel
            )
            
            statsDao.updatePetStats(nextStats)
            logDao.insertLog(CareLog(message = "You chased yellow butterflies with Lula! She pounces! (+30 Joy)", actionType = "PLAY"))
            
            setTemporaryState(LulaState.PLAYING, 2500)
        }
    }

    fun sleepLula() {
        viewModelScope.launch {
            val current = statsDao.getPetStats() ?: PetStats()
            if (current.energy >= 95) {
                logDao.insertLog(CareLog(message = "Lula isn't tired yet, she's full of bouncy energy! 🦁", actionType = "SYSTEM"))
                return@launch
            }

            // Stats updates: Fullness -20, thirst -12, energy restored to 100, happiness slightly adjusts
            val nextFullness = (current.hunger - 20).coerceAtLeast(10)
            val nextThirst = (current.thirst - 12).coerceAtLeast(5)
            val nextEnergy = 100
            val nextHappiness = (current.happiness - 5).coerceAtLeast(30)
            
            val addedBond = 10
            val nextBond = current.bond + addedBond
            val nextLevel = calculateLevel(nextBond)

            val nextStats = current.copy(
                hunger = nextFullness,
                thirst = nextThirst,
                energy = nextEnergy,
                happiness = nextHappiness,
                bond = nextBond,
                level = nextLevel
            )
            
            statsDao.updatePetStats(nextStats)
            logDao.insertLog(CareLog(message = "Lula curled up under the cool Baobab shade. Zzz... (+100 Energy)", actionType = "SLEEP"))
            
            setTemporaryState(LulaState.SLEEPING, 4000)
        }
    }

    fun petLula() {
        viewModelScope.launch {
            val current = statsDao.getPetStats() ?: PetStats()
            
            // Stats updates: Happiness +15, Bond +10
            val nextHappiness = (current.happiness + 15).coerceAtMost(100)
            
            val addedBond = 10
            val nextBond = current.bond + addedBond
            val nextLevel = calculateLevel(nextBond)

            val nextStats = current.copy(
                happiness = nextHappiness,
                bond = nextBond,
                level = nextLevel
            )
            
            statsDao.updatePetStats(nextStats)
            logDao.insertLog(CareLog(message = "You rubbed Lula's soft golden chin. She is purring! (+10 Bond)", actionType = "PET"))
            
            setTemporaryState(LulaState.LOVED, 2500)
        }
    }

    fun hydrateLula() {
        viewModelScope.launch {
            val current = statsDao.getPetStats() ?: PetStats()
            
            // Stats updates: Hydration (Thirst) +35, energy +5, happiness +5
            val nextThirst = (current.thirst + 35).coerceAtMost(100)
            val nextEnergy = (current.energy + 5).coerceAtMost(100)
            val nextHappiness = (current.happiness + 5).coerceAtMost(100)
            
            val addedBond = 10
            val nextBond = current.bond + addedBond
            val nextLevel = calculateLevel(nextBond)

            val nextStats = current.copy(
                thirst = nextThirst,
                energy = nextEnergy,
                happiness = nextHappiness,
                bond = nextBond,
                level = nextLevel
            )
            
            statsDao.updatePetStats(nextStats)
            logDao.insertLog(CareLog(message = "You offered Lula cool savanna spring water! Lap lap lap! (+35 Hydration)", actionType = "DRINK"))
            
            setTemporaryState(LulaState.EATING, 2500)
        }
    }

    fun passSimulationTime() {
        // Fast-forwards time by 3 hours, decaying stats, to mock the virtual pet gameplay
        viewModelScope.launch {
            val current = statsDao.getPetStats() ?: PetStats()
            val nextFullness = (current.hunger - 30).coerceAtLeast(0)
            val nextThirst = (current.thirst - 30).coerceAtLeast(0)
            val nextEnergy = (current.energy - 25).coerceAtLeast(0)
            val nextHappiness = (current.happiness - 20).coerceAtLeast(10)

            statsDao.updatePetStats(
                current.copy(
                    hunger = nextFullness,
                    thirst = nextThirst,
                    energy = nextEnergy,
                    happiness = nextHappiness
                )
            )
            logDao.insertLog(CareLog(message = "The sun sets on the savanna savanna. Lula is hungry and sleepy. 🌅", actionType = "SYSTEM"))
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            statsDao.updatePetStats(PetStats())
            logDao.clearLogs()
            chatDao.clearChat()
            logDao.insertLog(CareLog(message = "The savanna game has been reset! Lula is ready to play again.", actionType = "SYSTEM"))
        }
    }

    // Helper level system
    private fun calculateLevel(bondPoints: Int): Int {
        return when {
            bondPoints < 150 -> 1    // "New Buddy"
            bondPoints < 400 -> 2    // "Savanna Companion"
            bondPoints < 750 -> 3    // "Lionheart Protector"
            bondPoints < 1200 -> 4   // "Majestic Savanna Elder"
            else -> 5                // "Eternal Bond Bestie"
        }
    }

    val bondLevelTitle: String
        get() {
            val bond = petStatsFlow.value.bond
            return when {
                bond < 150 -> "Savanna Companion"
                bond < 400 -> "Safari Bestie 🌿"
                bond < 750 -> "Pride Protector 🦁"
                bond < 1200 -> "Lionheart Hero 👑"
                else -> "Eternal Savanna Elder ✨"
            }
        }

    // --- Chat Actions ---

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            // Write User turn to room
            val userMsg = ChatMessage(sender = "user", text = text)
            chatDao.insertMessage(userMsg)

            // Trigger AI response loading
            _isChatLoading.value = true

            // Send actual content (sending history together for memory logic)
            val currentHistory = chatMessagesFlow.value
            val lulaReply = LulaChatService.talkToLula(text, currentHistory)

            // Store Model reply
            val systemMsg = ChatMessage(sender = "lula", text = lulaReply)
            chatDao.insertMessage(systemMsg)

            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearChat()
        }
    }
}
