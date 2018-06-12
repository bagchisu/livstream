package io.livstream

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.livstream.model.Liv

import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.widget.helper.ItemTouchHelper
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var editPosition: Int = -1;

    private lateinit var dataset: ArrayList<Liv>

    val ADD_LIV_REQUEST = 1  // The request code
    val EDIT_LIV_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        loadDataset()

        fab.setOnClickListener { _ ->
            run({
                val intent = Intent(this, LivActivity::class.java)
                startActivityForResult(intent, ADD_LIV_REQUEST)
            })
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = LivAdapter(dataset)

        recyclerView = findViewById<RecyclerView>(R.id.liv_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

        recyclerView.addOnItemTouchListener(
            RecyclerItemListener(applicationContext, recyclerView,
                object : RecyclerItemListener.RecyclerTouchListener {
                    override fun onClickItem(v: View, position: Int) {
                        println("On Click Item interface $position")
                        val intent = Intent(v.context, LivActivity::class.java)
                        intent.putExtra("liv_obj", dataset[position])
                        editPosition = position
                        startActivityForResult(intent, EDIT_LIV_REQUEST)
                    }
                    override fun onLongClickItem(v: View, position: Int) {
                        println("On Long Click Item interface $position")
                    }
                }))

        val swipeController = SwipeController(object: SwipeControllerActions {
            override fun onRightSwipe(position: Int) {
                val done = dataset.removeAt(position)
                viewAdapter.notifyItemRemoved(position)
                dataset.add(done)
                viewAdapter.notifyItemInserted(dataset.size-1)
                println("marked ${done.title} as done and moved to end")
                saveDataset()
            }

            override fun onLeftSwipe(position: Int) {
                val done = dataset.removeAt(position)
                viewAdapter.notifyItemRemoved(position)
                println("deleted ${done.title} from list")
                saveDataset()
            }
        })
        val itemTouchhelper = ItemTouchHelper(swipeController)
        itemTouchhelper.attachToRecyclerView(recyclerView)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Check which request we're responding to
        when(requestCode) {
            ADD_LIV_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    println("Add Liv success")
                    val newLiv = data.extras.getSerializable("liv_obj") as Liv
                    println("Found new liv with title: " + newLiv.title)
                    dataset.add(newLiv)
                    viewAdapter.notifyItemInserted(dataset.size - 1)
                    saveDataset()
                }
            }
            EDIT_LIV_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    println("Edit Liv success")
                    val editedLiv = data.extras.getSerializable("liv_obj") as Liv
                    dataset[editPosition] = editedLiv
                    println("Edited liv at position $editPosition")
                    viewAdapter.notifyItemChanged(editPosition)
                    editPosition = -1
                    saveDataset()
                }
            }
        }
    }

    private fun loadDataset() {
        try {
            openFileInput("livStream").use {
                ObjectInputStream(it).use {
                    dataset = it.readObject() as ArrayList<Liv>
                    return
                }
            }
        } catch (e: IOException) {
            println(e.message)
        }
        dataset = ArrayList<Liv>()
    }

    private fun saveDataset() {
        try {
            openFileOutput("livStream", Context.MODE_PRIVATE).use {
                ObjectOutputStream(it).use {
                    it.writeObject(dataset)
                }
            }
        } catch (e: IOException) {
            println(e.message)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
