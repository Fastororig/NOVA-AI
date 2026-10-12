// Jetpack Compose экран с кнопкой «Обновить список моделей»
FilledTonalButton(
    onClick = { viewModel.refreshModels() },
    enabled = !viewModel.isLoadingModels
) {
    if (viewModel.isLoadingModels) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp))
        Text("Загрузка...")
    } else {
        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
        Text("Обновить список")
    }
}