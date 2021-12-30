package cat.balrog.simplelist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import cat.balrog.glassjoy.InputGesture
import cat.balrog.katzrdum.Katzrdum
import cat.balrog.katzrdum.StringField
import cat.balrog.simplelist.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private companion object {
        private const val KEY_ITEM = "item"
        private const val TIMEOUT_EXIT = 1000L
    }
    private lateinit var binding: ActivityMainBinding
    private val katzrdum by lazy { Katzrdum(StringField(KEY_ITEM, getString(R.string.add_item))) }
    private var joyRepeatJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }
    private var connectCallback: (()->Unit)? = null
    private var backTriggered = Long.MIN_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val listAdapter = ListAdapter()
        binding.list.adapter = listAdapter
        listAdapter.loadItems(this)
        katzrdum.showPromptCallback = { code, connect ->
            binding.connect.text = getString(R.string.tap_to_connect, code)
            binding.connect.isVisible = true
            connectCallback = connect
        }
        katzrdum.listen(this).asLiveData().observe(this) { (key, value) ->
            if (key == KEY_ITEM) {
                listAdapter.addItem(value.toString())
                listAdapter.saveItems(this)
            }
        }
        binding.input.inputs.asLiveData().observe(this) {
            when (it) {
                InputGesture.JoyUp -> scheduleJoyRepeat {
                    binding.list.scrollToPosition(listAdapter.selectionUp())
                }
                InputGesture.JoyDown -> scheduleJoyRepeat {
                    binding.list.scrollToPosition(listAdapter.selectionDown())
                }
                InputGesture.JoyNeutral, InputGesture.JoyLeft, InputGesture.JoyRight -> {
                    joyRepeatJob = null
                }
                InputGesture.SwipeLeft, InputGesture.SwipeRight -> {
                    listAdapter.removeSelected()
                    listAdapter.saveItems(this)
                }
                InputGesture.SwipeDown -> back()
                InputGesture.Tap -> {
                    connectCallback?.invoke()
                    binding.connect.isVisible = false
                    connectCallback = null
                }
                else -> { /* no-op*/ }
            }
        }
    }

    override fun onDestroy() {
        joyRepeatJob = null
        super.onDestroy()
    }

    private fun scheduleJoyRepeat(block: ()->Unit) {
        joyRepeatJob = lifecycleScope.launch {
            block()
            delay(1000)
            while (true) {
                block()
                delay(500)
            }
        }
    }

    private fun back() {
        if (binding.connect.isVisible) {
            connectCallback = null
            binding.connect.isVisible = false
        } else {
            val now = SystemClock.elapsedRealtime()
            if (backTriggered + TIMEOUT_EXIT < now) {
                backTriggered = now
                Toast.makeText(this, R.string.swipe_to_exit, Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }
}