package lutech.intern.noteapp.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import lutech.intern.noteapp.R
import lutech.intern.noteapp.common.PreferencesManager
import lutech.intern.noteapp.constant.FragmentTag
import lutech.intern.noteapp.constant.SortNoteMode
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.databinding.ActivityMainBinding
import lutech.intern.noteapp.databinding.DialogSortOptionsBinding
import lutech.intern.noteapp.event.ClearNotesSelectedEvent
import lutech.intern.noteapp.event.DeleteNoteEvent
import lutech.intern.noteapp.event.LoadNotesEvent
import lutech.intern.noteapp.event.SearchNoteEvent
import lutech.intern.noteapp.event.SelectedAllNotesEvent
import lutech.intern.noteapp.ui.BackupActivity
import lutech.intern.noteapp.ui.HelpActivity
import lutech.intern.noteapp.ui.note.NotesFragment
import lutech.intern.noteapp.ui.PrivacyPolicyActivity
import lutech.intern.noteapp.ui.SettingsActivity
import lutech.intern.noteapp.ui.TrashFragment
import lutech.intern.noteapp.ui.category.CategoriesFragment
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()
    var navMenuItemIdSelected: Int? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initToolbar()
        handleEvent()
        observeCategoriesUpdate()

        if (savedInstanceState == null) {
            loadFragment(NotesFragment.newInstance(), FragmentTag.TAG_FRAGMENT_NOTES.toString())
            setToolbarTitle(getString(R.string.app_name), null)
            navMenuItemIdSelected = R.id.menu_notes
        }
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setToolbarTitle(title: String, subTitle: CharSequence? = null) {
        binding.toolbar.run {
            this.title = title
            this.subtitle = subTitle
        }
    }

    private fun handleEvent() {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_notes -> {
                    loadFragment(
                        NotesFragment.newInstance(),
                        FragmentTag.TAG_FRAGMENT_NOTES.toString()
                    )
                    setToolbarTitle(getString(R.string.app_name), null)
                    navMenuItemIdSelected = R.id.menu_notes
                    EventBus.getDefault().post(LoadNotesEvent())
                    invalidateOptionsMenu()
                }

                R.id.menu_uncategorized -> {
                    loadFragment(
                        NotesFragment.newInstance(),
                        FragmentTag.TAG_FRAGMENT_NOTES.toString()
                    )
                    setToolbarTitle(getString(R.string.app_name), item.title)
                    navMenuItemIdSelected = R.id.menu_uncategorized
                    EventBus.getDefault().post(LoadNotesEvent())
                    invalidateOptionsMenu()
                }

                R.id.menu_edit_categories -> {
                    loadFragment(
                        CategoriesFragment.newInstance(),
                        FragmentTag.TAG_FRAGMENT_CATEGORIES.toString()
                    )
                    setToolbarTitle(getString(R.string.categories), null)
                    EventBus.getDefault().post(LoadNotesEvent())
                    binding.toolbar.menu.clear()
                }

                R.id.menu_backup -> {
                    val intent = Intent(this, BackupActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_trash -> {
                    loadFragment(
                        TrashFragment.newInstance(),
                        FragmentTag.TAG_FRAGMENT_TRASH.toString()
                    )
                    setToolbarTitle(getString(R.string.trash), null)
                }

                R.id.menu_setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_rate -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_app)))
                    startActivity(intent)
                }

                R.id.menu_help -> {
                    val intent = Intent(this, HelpActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_privacy_policy -> {
                    val intent = Intent(this, PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                }

                else -> {
                    if (item.groupId == R.id.group_categories) {
                        loadFragment(
                            NotesFragment.newInstance(),
                            FragmentTag.TAG_FRAGMENT_NOTES.toString()
                        )
                        setToolbarTitle(getString(R.string.app_name), item.title)
                        navMenuItemIdSelected = item.itemId
                        EventBus.getDefault().post(LoadNotesEvent())
                        invalidateOptionsMenu()
                    }
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        var existingFragment = fragmentManager.findFragmentByTag(tag)
        if (existingFragment == null) {
            existingFragment = fragment
            fragmentTransaction.add(R.id.container, existingFragment, tag)
        }

        for (frag in fragmentManager.fragments) {
            if (frag != existingFragment) {
                fragmentTransaction.hide(frag)
            }
        }

        fragmentTransaction.show(existingFragment)
        fragmentTransaction.commitNow()
    }

    private fun observeCategoriesUpdate() {
        mainViewModel.categories.observe(this) { categories ->
            updateCategoriesGroupItem(categories)
        }
    }

    private fun updateCategoriesGroupItem(categories: List<Category>) {
        val menu = binding.navigationView.menu
        val categoriesGroupItem = menu.findItem(R.id.group_categories)
        val subMenu = categoriesGroupItem.subMenu

        subMenu?.let {
            it.clear()
            categories.forEach { category ->
                it.add(
                    R.id.group_categories,
                    category.categoryId.toInt(),
                    Menu.NONE,
                    category.name
                ).setIcon(R.drawable.ic_categorized)
            }
        }

        if (categories.isNotEmpty()) {
            menuInflater.inflate(R.menu.menu_item_uncategorized, subMenu)
        }
        menuInflater.inflate(R.menu.menu_item_edit_categories, subMenu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Search event
        val searchItem = menu?.findItem(R.id.menu_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                EventBus.getDefault().post(SearchNoteEvent(newText))
                return true
            }
        })


        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                binding.toolbar.menu.findItem(R.id.menu_sort).isVisible = false
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                invalidateOptionsMenu()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort -> {
                showSortOptionDialog()
            }

            R.id.menu_select_all -> {
                EventBus.getDefault().post(SelectedAllNotesEvent())
            }

            R.id.menu_import -> {
                Toast.makeText(this, "Import file", Toast.LENGTH_SHORT).show()
            }

            R.id.menu_export -> {
                Toast.makeText(this, "Export file", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSortOptionDialog() {
        val dialogBinding = DialogSortOptionsBinding.inflate(layoutInflater)
        val initialSortModeRadioId = getRadioIdBySortNoteMode()
        dialogBinding.sortGroup.findViewById<RadioButton>(initialSortModeRadioId).isChecked = true

        var selectedSortNoteMode: SortNoteMode? = null

        val dialog = AlertDialog.Builder(this).apply {
            setView(dialogBinding.root)
            setPositiveButton(R.string.ok) { _, _ ->
                selectedSortNoteMode?.let {
                    PreferencesManager.setSortMode(it)
                    EventBus.getDefault().post(LoadNotesEvent())
                }
            }
            setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        }.create()

        dialog.show()

        dialogBinding.sortGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedSortNoteMode = getSortNoteModeByRadioId(checkedId)
        }
    }

    private fun getSortNoteModeByRadioId(checkedId: Int): SortNoteMode {
        return when (checkedId) {
            R.id.radio_edit_date_newest -> SortNoteMode.EDIT_DATE_NEWEST
            R.id.radio_edit_date_oldest -> SortNoteMode.EDIT_DATE_OLDEST
            R.id.radio_creation_date_newest -> SortNoteMode.CREATION_DATE_NEWEST
            R.id.radio_creation_date_oldest -> SortNoteMode.CREATION_DATE_OLDEST
            R.id.radio_title_a_z -> SortNoteMode.TITLE_A_Z
            R.id.radio_title_z_a -> SortNoteMode.TITLE_Z_A
            R.id.radio_color -> SortNoteMode.COLOR
            else -> SortNoteMode.EDIT_DATE_OLDEST
        }
    }

    private fun getRadioIdBySortNoteMode(): Int {
        return when (PreferencesManager.getSortMode()) {
            SortNoteMode.EDIT_DATE_NEWEST.name -> R.id.radio_edit_date_newest
            SortNoteMode.EDIT_DATE_OLDEST.name -> R.id.radio_edit_date_oldest
            SortNoteMode.CREATION_DATE_NEWEST.name -> R.id.radio_creation_date_newest
            SortNoteMode.CREATION_DATE_OLDEST.name -> R.id.radio_creation_date_oldest
            SortNoteMode.TITLE_A_Z.name -> R.id.radio_title_a_z
            SortNoteMode.TITLE_Z_A.name -> R.id.radio_title_z_a
            SortNoteMode.COLOR.name -> R.id.radio_color
            else -> R.id.radio_edit_date_newest
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.menu_delete, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_focus_all -> {
                    EventBus.getDefault().post(SelectedAllNotesEvent())
                    return true

                }

                R.id.menu_delete -> {
                    EventBus.getDefault().post(DeleteNoteEvent())
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            EventBus.getDefault().post(ClearNotesSelectedEvent())
        }
    }

    fun openActionMode() {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback)!!
        }
    }

    fun setTitleActionMode(title: String) {
        actionMode?.title = title
    }

    fun finishActionMode() {
        actionMode?.finish()
    }
}