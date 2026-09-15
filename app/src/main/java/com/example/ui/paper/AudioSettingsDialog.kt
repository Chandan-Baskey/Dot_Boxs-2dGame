package com.example.ui.paper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSettings
import com.example.ui.theme.BlueInk
import com.example.ui.theme.InkDark
import com.example.ui.theme.InkMedium
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperCard
import com.example.ui.theme.PaperSurface

private enum class SettingsView {
    MAIN,
    MUSIC_AND_SOUND,
    HOW_TO_PLAY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsDialog(
    settings: AudioSettings,
    onMusicToggled: (Boolean) -> Unit,
    onSfxToggled: (Boolean) -> Unit,
    onMusicVolumeChanged: (Float) -> Unit = {},
    onSfxVolumeChanged: (Float) -> Unit = {},
    onTestSfx: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentView by remember { mutableStateOf(SettingsView.MAIN) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("audio_settings_dialog")
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .heightIn(max = 580.dp)
                .shadow(8.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = PaperCard,
            border = BorderStroke(2.dp, PaperBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentView) {
                    SettingsView.MAIN -> {
                        // Main Settings Menu with only two options: "Music & Sound" and "How to Play"
                        MainSettingsView(
                            onOpenMusicAndSound = { currentView = SettingsView.MUSIC_AND_SOUND },
                            onOpenHowToPlay = { currentView = SettingsView.HOW_TO_PLAY },
                            onClose = onDismiss
                        )
                    }
                    SettingsView.MUSIC_AND_SOUND -> {
                        // Music & Sound sub-screen
                        MusicAndSoundSettingsView(
                            settings = settings,
                            onMusicToggled = onMusicToggled,
                            onSfxToggled = onSfxToggled,
                            onTestSfx = onTestSfx,
                            onBack = { currentView = SettingsView.MAIN },
                            onClose = onDismiss
                        )
                    }
                    SettingsView.HOW_TO_PLAY -> {
                        // How to Play sub-screen
                        HowToPlaySettingsView(
                            onBack = { currentView = SettingsView.MAIN },
                            onClose = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainSettingsView(
    onOpenMusicAndSound: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    onClose: () -> Unit
) {
    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BlueInk.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = BlueInk,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "SETTINGS",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = InkDark,
                letterSpacing = 1.2.sp
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_audio_settings")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = InkMedium
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Option 1: Music & Sound
    SettingsMenuOptionCard(
        title = "Music & Sound",
        subtitle = "Background music & sound effects",
        icon = Icons.Default.VolumeUp,
        onClick = onOpenMusicAndSound,
        testTag = "settings_option_music_and_sound"
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Option 2: How to Play
    SettingsMenuOptionCard(
        title = "How to Play",
        subtitle = "Rules, scoring & strategies",
        icon = Icons.Default.MenuBook,
        onClick = onOpenHowToPlay,
        testTag = "settings_option_how_to_play"
    )

    Spacer(modifier = Modifier.height(22.dp))

    // Close Button
    PaperButton(
        text = "CLOSE",
        onClick = onClose,
        isPrimary = true,
        inkColor = BlueInk,
        testTag = "done_settings_button",
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    )
}

@Composable
private fun SettingsMenuOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = PaperSurface,
        border = BorderStroke(1.dp, PaperBorder.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PaperBorder.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BlueInk,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = InkMedium
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Open",
                tint = InkMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MusicAndSoundSettingsView(
    settings: AudioSettings,
    onMusicToggled: (Boolean) -> Unit,
    onSfxToggled: (Boolean) -> Unit,
    onTestSfx: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    // Header with Back button and Title
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_to_settings")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = InkDark,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Music & Sound",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = InkDark
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_audio_settings")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = InkMedium
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Background Music Toggle Card
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = PaperSurface,
        border = BorderStroke(1.dp, PaperBorder.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (settings.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    contentDescription = null,
                    tint = if (settings.musicEnabled) BlueInk else InkMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Background Music",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (settings.musicEnabled) "ON" else "OFF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (settings.musicEnabled) BlueInk else InkMuted
                    )
                }
            }

            Switch(
                checked = settings.musicEnabled,
                onCheckedChange = onMusicToggled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BlueInk,
                    uncheckedThumbColor = InkMuted,
                    uncheckedTrackColor = PaperBorder
                ),
                modifier = Modifier.testTag("switch_music")
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Game Sound Effects Toggle Card
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = PaperSurface,
        border = BorderStroke(1.dp, PaperBorder.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (settings.sfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = null,
                    tint = if (settings.sfxEnabled) BlueInk else InkMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Game Sound Effects",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (settings.sfxEnabled) "ON" else "OFF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (settings.sfxEnabled) BlueInk else InkMuted
                    )
                }
            }

            Switch(
                checked = settings.sfxEnabled,
                onCheckedChange = onSfxToggled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BlueInk,
                    uncheckedThumbColor = InkMuted,
                    uncheckedTrackColor = PaperBorder
                ),
                modifier = Modifier.testTag("switch_sfx")
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Back button
    PaperButton(
        text = "BACK",
        onClick = onBack,
        isPrimary = true,
        inkColor = BlueInk,
        testTag = "done_settings_button",
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    )
}

@Composable
private fun HowToPlaySettingsView(
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("back_to_settings")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = InkDark,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "How to Play",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = InkDark
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_audio_settings")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = InkMedium
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Rules Content
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = PaperSurface,
        border = BorderStroke(1.dp, PaperBorder.copy(alpha = 0.85f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            RuleItem(
                number = "1",
                title = "Draw a Line",
                description = "On your turn, tap or drag between two adjacent dots to draw a pencil stroke."
            )
            Spacer(modifier = Modifier.height(12.dp))
            RuleItem(
                number = "2",
                title = "Capture a Box",
                description = "Draw the 4th side of any 1×1 square to complete it with your ink color and score 1 point."
            )
            Spacer(modifier = Modifier.height(12.dp))
            RuleItem(
                number = "3",
                title = "Earn an Extra Turn",
                description = "Every time you close a box, you immediately get another turn. Chain captures together!"
            )
            Spacer(modifier = Modifier.height(12.dp))
            RuleItem(
                number = "4",
                title = "Win the Game",
                description = "The game concludes when all boxes on the grid are claimed. The player with the highest score wins!"
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Back Button
    PaperButton(
        text = "BACK",
        onClick = onBack,
        isPrimary = true,
        inkColor = BlueInk,
        testTag = "done_settings_button",
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    )
}

@Composable
private fun RuleItem(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(BlueInk.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BlueInk
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = InkMedium,
                lineHeight = 17.sp
            )
        }
    }
}
