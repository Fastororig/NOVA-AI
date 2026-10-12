// Выбор нескольких файлов на Android 7.0+ (API 24)
val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetMultipleContents()
) { uris ->
    if (uris.isNotEmpty()) viewModel.addFiles(context, uris)
}

// Кнопка-скрепка рядом с полем ввода
IconButton(
    onClick = { filePickerLauncher.launch("*/*") }
) {
    Icon(Icons.Default.AttachFile, contentDescription = "Прикрепить файлы")
}

// Горизонтальный список прикреплённых файлов с миниатюрами и крестиком удаления
if (attachedFiles.isNotEmpty()) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        attachedFiles.forEach { file ->
            AttachmentChip(file = file, onRemove = { viewModel.removeAttachment(file.id) })
        }
    }
}