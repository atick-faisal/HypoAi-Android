package dev.atick.hypoai.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.atick.core.extensions.getBitmap
import dev.atick.core.ui.components.TopBar
import dev.atick.core.ui.extensions.getTmpFileUri
import dev.atick.core.ui.utils.TakePictureWithUriReturnContract
import dev.atick.hypoai.BuildConfig
import dev.atick.hypoai.ui.home.components.HomeContent
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    onVisitWebsiteClick: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val homeUiState by homeViewModel.homeUiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    homeUiState.toastMessage?.let {
        val errorMessage = it.asString()
        LaunchedEffect(homeUiState) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    snackbarHost.showSnackbar(errorMessage)
                    homeViewModel.clearError()
                }
            }
        }
    }

    val context = LocalContext.current

    val takePhotoLauncher = rememberLauncherForActivityResult(
        TakePictureWithUriReturnContract()
    ) { (isSuccess, uri) ->
        if (isSuccess) {
            homeViewModel.setInputImageUri(uri)
            homeViewModel.setInputImageBitmap(uri.getBitmap(context)?.asImageBitmap())
        }
    }

    val openGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            homeViewModel.setInputImageUri(uri)
            homeViewModel.setInputImageBitmap(uri.getBitmap(context)?.asImageBitmap())
        }
    }

    Scaffold(
        modifier = Modifier.background(Color(0xFF000000)),
        topBar = { TopBar() },
        snackbarHost = { SnackbarHost(hostState = snackbarHost) }
    ) { paddingValues ->
        HomeContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            homeUiState = homeUiState,
            onImageCaptureClick = {
                takePhotoLauncher.launch(
                    context.getTmpFileUri(BuildConfig.APPLICATION_ID)
                )
            },
            onSelectAnotherImageClick = { homeViewModel.clearAnalysisResult() },
            onOpenGalleryClick = {
                openGalleryLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            onHypospadiasAnalysisClick = { homeViewModel.uploadImageForHypospadiasAnalysis() },
            onCurvatureAnalysisClick = { homeViewModel.uploadImageForCurvatureAnalysis() },
            onVisitWebsiteClick = onVisitWebsiteClick
        )
    }
}