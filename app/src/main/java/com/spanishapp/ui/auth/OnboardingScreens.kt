package com.spanishapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun NameEntryScreen(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Как тебя зовут?", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя или никнейм") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = { 
                viewModel.updateName(name)
                navController.navigate("age_selection")
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = name.isNotBlank()
        ) {
            Text("Далее")
        }
    }
}

@Composable
fun AgeSelectionScreen(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    var age by remember { mutableStateOf(18) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Сколько тебе лет?", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("Это поможет нам настроить программу обучения", color = Color.Gray, textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(48.dp))
        
        Text("$age лет", fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        
        Slider(
            value = age.toFloat(),
            onValueChange = { age = it.toInt() },
            valueRange = 5f..99f,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(Modifier.height(64.dp))
        
        Button(
            onClick = { 
                viewModel.updateAge(age)
                navController.navigate("reason_selection")
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Далее")
        }
    }
}

@Composable
fun ReasonSelectionScreen(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    val reasons = listOf(
        "✈️ Путешествия",
        "💼 Работа / Карьера",
        "🧠 Саморазвитие",
        "🎓 Учеба",
        "❤️ Общение / Семья",
        "🎸 Хобби / Культура"
    )
    var selectedReason by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        Text("Зачем ты учишь испанский?", fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(32.dp))
        
        reasons.forEach { reason ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { selectedReason = reason },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedReason == reason) MaterialTheme.colorScheme.primaryContainer else Color.White
                ),
                border = if (selectedReason == reason) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(0.4f))
            ) {
                Text(reason, modifier = Modifier.padding(20.dp), fontSize = 18.sp)
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = { 
                selectedReason?.let {
                    viewModel.updateReason(it)
                    navController.navigate("level_selection")
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = selectedReason != null
        ) {
            Text("Далее")
        }
    }
}
