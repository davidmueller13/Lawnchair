package app.lawnchair.ui.preferences.about

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.create

class AboutViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val nightlyBuildsRepository = NightlyBuildsRepository(
        applicationContext = application,
        api = gitHubApiRetrofit.create(),
    )

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState = _uiState.asStateFlow()
    val updateState = nightlyBuildsRepository.updateState

    init {
        _uiState.update {
            it.copy(
                versionName = BuildConfig.VERSION_NAME,
                commitHash = BuildConfig.COMMIT_HASH,
                coreTeam = team,
                supportAndPr = supportAndPr,
                links = links,
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            val activeContributors = fetchActiveContributors()
            val updatedCoreTeam = _uiState.value.coreTeam.map { member ->
                val status = if (member.githubUsername != null && activeContributors.contains(member.githubUsername.lowercase())) ContributorStatus.Active else ContributorStatus.Idle
                member.copy(status = status)
            }
            _uiState.update { it.copy(coreTeam = updatedCoreTeam) }
        }

        if (BuildConfig.APPLICATION_ID.contains("nightly")) {
            nightlyBuildsRepository.checkForUpdate()
            viewModelScope.launch {
                nightlyBuildsRepository.updateState.collect { state ->
                    _uiState.update { it.copy(updateState = state) }
                }
            }
        }
    }

    fun onEvent(event: AboutEvent) {
        when (event) {
            is AboutEvent.OnDownloadClicked -> nightlyBuildsRepository.downloadUpdate()
            is AboutEvent.OnInstallClicked -> nightlyBuildsRepository.installUpdate(event.file)
        }
    }

    private suspend fun fetchActiveContributors(): Set<String> {
        return runCatching {
            nightlyBuildsRepository.api.getRepositoryEvents("LawnchairLauncher", "lawnchair")
                .map { it.actor.login.lowercase() }
                .toSet()
        }.getOrDefault(emptySet())
    }
    companion object {
        private val team = listOf(
            TeamMember(
                name = "Amogh Lele",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/31761843",
                socialUrl = "https://www.linkedin.com/in/amogh-lele/",
            ),
            TeamMember(
                name = "Antonio J. Roa Valverde",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/914983",
                socialUrl = "https://x.com/6020peaks",
            ),
            TeamMember(
                name = "David Sn",
                role = Role.DevOps,
                photoUrl = "https://i.imgur.com/b65akTl.png",
                socialUrl = "https://codebucket.de",
            ),
            TeamMember(
                name = "Goooler",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/10363352",
                socialUrl = "https://github.com/Goooler",
                githubUsername = "Goooler",
            ),
            TeamMember(
                name = "Harsh Shandilya",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/13348378",
                socialUrl = "https://github.com/msfjarvis",
            ),
            TeamMember(
                name = "John Andrew Camu (MrSluffy)",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/36076410",
                socialUrl = "https://github.com/MrSluffy",
                githubUsername = "MrSluffy",
            ),
            TeamMember(
                name = "Kshitij Gupta",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/18647641",
                socialUrl = "https://x.com/Agent_Fabulous",
            ),
            TeamMember(
                name = "Manuel Lorenzo",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/183264",
                socialUrl = "https://x.com/noloman",
            ),
            TeamMember(
                name = "paphonb",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/8080853",
                socialUrl = "https://x.com/paphonb",
            ),
            TeamMember(
                name = "raphtlw",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/47694127",
                socialUrl = "https://x.com/raphtlw",
            ),
            TeamMember(
                name = "Rhyse Simpson",
                role = Role.QuickSwitchMaintenance,
                photoUrl = "https://avatars.githubusercontent.com/u/7065700",
                socialUrl = "https://x.com/skittles9823",
            ),
            TeamMember(
                name = "SuperDragonXD",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/70206496",
                socialUrl = "https://github.com/SuperDragonXD",
                githubUsername = "SuperDragonXD",
            ),
            TeamMember(
                name = "Yasan Glass",
                role = Role.Development,
                photoUrl = "https://avatars.githubusercontent.com/u/41836211",
                socialUrl = "https://yasan.glass",
                githubUsername = "yasanglass",
            ),
        )

        private val links = listOf(
            Link(
                iconResId = R.drawable.ic_new_releases,
                labelResId = R.string.news,
                url = "https://t.me/lawnchairci",
            ),
            Link(
                iconResId = R.drawable.ic_help,
                labelResId = R.string.support,
                url = "https://t.me/lccommunity",
            ),
            Link(
                iconResId = R.drawable.ic_x_twitter,
                labelResId = R.string.x_twitter,
                url = "https://x.com/lawnchairapp",
            ),
            Link(
                iconResId = R.drawable.ic_github,
                labelResId = R.string.github,
                url = "https://github.com/LawnchairLauncher/lawnchair",
            ),
            Link(
                iconResId = R.drawable.ic_discord,
                labelResId = R.string.discord,
                url = "https://discord.com/invite/3x8qNWxgGZ",
            ),
        )

        private val supportAndPr = listOf(
            TeamMember(
                name = "Daniel Souza",
                role = Role.Support,
                photoUrl = "https://avatars.githubusercontent.com/u/32078304",
                socialUrl = "https://github.com/DanGLES3",
            ),
            TeamMember(
                name = "Giuseppe Longobardo",
                role = Role.Support,
                photoUrl = "https://avatars.githubusercontent.com/u/49398464",
                socialUrl = "https://github.com/joseph-20",
            ),
            TeamMember(
                name = "Rik Koedoot",
                role = Role.SupportAndPr,
                photoUrl = "https://avatars.githubusercontent.com/u/29402532",
                socialUrl = "https://x.com/rikkoedoot",
            ),
        )
    }
}
