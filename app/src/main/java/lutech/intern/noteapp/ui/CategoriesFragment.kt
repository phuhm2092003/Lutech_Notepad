package lutech.intern.noteapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import lutech.intern.noteapp.R

class CategoriesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    companion object {
        fun newInstance() = CategoriesFragment()
    }
}