package com.poco.launcher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poco.launcher.core.model.AppItem
import com.poco.launcher.core.model.FolderItem
import com.poco.launcher.ui.components.SquircleIconView
import com.poco.launcher.ui.theme.IosColors

@Composable
fun FolderDialog(
    folder: FolderItem?,
    apps: List<AppItem>,
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onRenameFolder: (folderId: String, newTitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (folder == null) return

    val isDark = isSystemInDarkTheme()
    val backdropColor = if (isDark) Color(0xB3000000) else Color(0x80000000)
    val cardBg = if (isDark) Color(0xE62C2C2E) else Color(0xE6FFFFFF)
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
    val textColor = if (isDark) Color.White else Color.Black

    var folderTitle by remember(folder.id) { mutableStateOf(folder.title) }
    val focusManager = LocalFocusManager.current

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.85f, animationSpec = tween(240)),
        exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.85f, animationSpec = tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(backdropColor)
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                        onDismiss()
                    }
                }
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(330.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { focusManager.clearFocus() }
                    }
            ) {
                // Folder Title Header (Editable on tap)
                BasicTextField(
                    value = folderTitle,
                    onValueChange = {
                        folderTitle = it
                        onRenameFolder(folder.id, it)
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(IosColors.Blue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                )

                // Translucent 3x3 Folder Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(0.4f))
                        .clip(RoundedCornerShape(32.dp))
                        .background(cardBg)
                        .border(0.6.dp, cardBorder, RoundedCornerShape(32.dp))
                        .padding(18.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                    ) {
                        items(
                            items = apps,
                            key = { it.id }
                        ) { app ->
                            SquircleIconView(
                                app = app,
                                iconSize = 58.dp,
                                showLabel = true,
                                textColor = textColor,
                                onClick = {
                                    onDismiss()
                                    onAppClick(app)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
