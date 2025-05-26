package com.cashbox.presentation.ui.penalties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cashbox.R
import com.cashbox.presentation.viewmodel.PenaltyViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PenaltiesFragment : Fragment() {

    private val viewModel: PenaltyViewModel by viewModels()
    private lateinit var penaltyAdapter: PenaltyAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var fabAddPenalty: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_penalties, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("PenaltiesFragment created")

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recycler_view_penalties)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyText = view.findViewById(R.id.text_empty)
        fabAddPenalty = view.findViewById(R.id.fab_add_penalty)

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        penaltyAdapter = PenaltyAdapter { penalty ->
            // Navigate to penalty details
            // This will be implemented in future versions
            Timber.d("Penalty clicked: ${penalty.id}")

            // Example navigation (uncomment when navigation is set up)
            // val action = PenaltiesFragmentDirections.actionToPenaltyDetail(penalty.id)
            // findNavController().navigate(action)
        }

        recyclerView.apply {
            adapter = penaltyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupFab() {
        fabAddPenalty.setOnClickListener {
            // Navigate to create penalty screen
            // This will be implemented in future versions
            Timber.d("Add penalty clicked")

            // Example navigation (uncomment when navigation is set up)
            // val action = PenaltiesFragmentDirections.actionToCreatePenalty()
            // findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        // Observe penalties
        viewModel.penalties.observe(viewLifecycleOwner) { penalties ->
            penaltyAdapter.submitList(penalties)
            emptyText.isVisible = penalties.isEmpty()
        }

        // Observe UI state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                progressBar.isVisible = uiState.isLoading

                uiState.errorMessage?.let { message ->
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}
