package cat.balrog.simplelist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import cat.balrog.glassjoy.InputGesture
import cat.balrog.katzrdum.Katzrdum
import cat.balrog.katzrdum.StringField
import cat.balrog.simplelist.databinding.ActivityMainBinding

private const val KEY_ITEM = "item"

class MainActivity : AppCompatActivity() {
    private val katzrdum by lazy { Katzrdum(StringField(KEY_ITEM, getString(R.string.add_item))) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val listAdapter = ListAdapter()
        binding.list.adapter = listAdapter
        listAdapter.loadItems(this)
        katzrdum.showPromptCallback = { _, connect -> connect() } // TODO: Proper in-app UI
        katzrdum.listen(this).asLiveData().observe(this) { (key, value) ->
            if (key == KEY_ITEM) {
                listAdapter.addItem(value.toString())
                listAdapter.saveItems(this)
            }
        }
        binding.input.inputs.asLiveData().observe(this) {
            when (it) {
                // TODO: Repeat selection movement while joy direction held
                InputGesture.JoyUp -> {
                    binding.list.scrollToPosition(listAdapter.selectionUp())
                }
                InputGesture.JoyDown -> {
                    binding.list.scrollToPosition(listAdapter.selectionDown())
                }
                InputGesture.SwipeLeft, InputGesture.SwipeRight -> {
                    listAdapter.removeSelected()
                    listAdapter.saveItems(this)
                }
                InputGesture.SwipeDown -> finish()
                else -> { /* no-op*/ }
            }
        }
    }
}