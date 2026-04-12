package com.omnimap.presentation.graph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.omnimap.presentation.graph.chat.IntelligentBottomSheet
import com.omnimap.presentation.graph.ui.OmniMapCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(viewModel: GraphViewModel) {
    val state by viewModel.state.collectAsState()
    
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            IntelligentBottomSheet(viewModel = viewModel, state = state)
        },
        sheetPeekHeight = 64.dp,
        sheetContainerColor = Color(0xFF2B2930), // MD3 Surface Container High
        content = { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // The main canvas
                OmniMapCanvas(viewModel = viewModel)
            }
        }
    )
}