package com.kbk.presentation

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.kbk.presentation.di.DependencyProvider
import com.kbk.presentation.keyboard.KeyboardAction
import com.kbk.presentation.keyboard.KeyboardScreen
import com.kbk.presentation.theme.KeyboardTheme
import com.kbk.presentation.keyboard.KeyboardViewModel

class KeystrokeImeService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private lateinit var viewModel: KeyboardViewModel

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val dependencyProvider = applicationContext as DependencyProvider

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return KeyboardViewModel(
                    dependencyProvider.biometricService,
                    dependencyProvider.motionRepository
                ) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[KeyboardViewModel::class.java]
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this).apply {
            setContent {
                KeyboardTheme {
                    KeyboardScreen(
                        viewModel = viewModel,
                        onAction = ::handleKeyboardAction
                    )
                }
            }
        }

        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return composeView
    }

    private fun handleKeyboardAction(action: KeyboardAction) {
        val inputConnection = currentInputConnection ?: return
        when (action) {
            is KeyboardAction.CommitText -> inputConnection.commitText(action.text, 1)
            is KeyboardAction.Delete -> inputConnection.deleteSurroundingText(1, 0)
            is KeyboardAction.Enter -> inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            is KeyboardAction.Space -> inputConnection.commitText(" ", 1)
            else -> {}
        }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        viewModel.startTracking()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        viewModel.stopTracking()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
