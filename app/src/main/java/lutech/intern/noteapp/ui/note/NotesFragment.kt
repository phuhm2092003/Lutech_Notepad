package lutech.intern.noteapp.ui.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.NoteAdapter
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.databinding.FragmentNotesBinding
import lutech.intern.noteapp.ui.main.MainActivity

class NotesFragment : Fragment() {
    private val binding by lazy { FragmentNotesBinding.inflate(layoutInflater) }
    private val notesViewModel: NotesViewModel by viewModels()
    private val noteAdapter by lazy { NoteAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        handleEvent()
        observeDataViewModel()
    }

    private fun initViews() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.notesRecyclerView.adapter = noteAdapter
    }

    private fun observeDataViewModel() {
        notesViewModel.noteWithCategories.observe(viewLifecycleOwner) { list ->
            (activity as MainActivity).getItemCurrentIdSelected()?.let { idMenu ->
                when(idMenu) {
                    R.id.menu_notes -> noteAdapter.submitList(list)
                    R.id.menu_uncategorized -> {
                        val noteWithCategoriesNew = mutableListOf<NoteWithCategories>()
                        list.forEach { noteWithCategories ->
                            val categories = noteWithCategories.categories
                            if(categories.isEmpty()) {
                                noteWithCategoriesNew.add(noteWithCategories)
                            }
                        }
                        noteAdapter.submitList(noteWithCategoriesNew)
                    }

                    else -> {
                        val noteWithCategoriesNew = mutableListOf<NoteWithCategories>()
                        list.forEach { noteWithCategories ->
                            val categories = noteWithCategories.categories
                            categories.forEach { category ->
                                if(idMenu == category.categoryId.toInt()) {
                                    noteWithCategoriesNew.add(noteWithCategories)
                                }
                            }
                        }
                        noteAdapter.submitList(noteWithCategoriesNew)
                    }
                }
            }
        }
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener {
            notesViewModel.insert(Note())
        }
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}