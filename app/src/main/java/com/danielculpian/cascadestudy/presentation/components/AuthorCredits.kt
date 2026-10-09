package com.danielculpian.cascadestudy.presentation.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

// Composable function to display clickable author credits linking to the author's GitHub profile
@Composable
fun AuthorCredits(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Text(
        text = "Creado por Daniel Culpian",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textDecoration = TextDecoration.Underline,
        modifier = modifier
            .clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/DanielCulpian".toUri()
                )
                context.startActivity(intent)
            }
            .padding(8.dp)
    )
}
