/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samples.storage.saf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.samples.storage.R
import com.samples.storage.databinding.FragmentSafBinding
import kotlinx.coroutines.launch

private const val DEFAULT_FILE_NAME = "SAF Demo File.txt"

class SafFragment : Fragment() {
    private var _binding: FragmentSafBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SafFragmentViewModel by viewModels()

    private val actionCreateDocument = registerForActivityResult(CreateDocument()) { uri ->
        val documentUri = uri ?: return@registerForActivityResult
        val documentFile = DocumentFile.fromSingleUri(requireContext(), documentUri)
            ?: return@registerForActivityResult
        Log.d("SafFragment", "Created: ${documentFile.name}, type ${documentFile.type}")
    }

    private val actionOpenDocument = registerForActivityResult(OpenDocument()) { uri ->
        val documentUri = uri ?: return@registerForActivityResult
        val documentFile = DocumentFile.fromSingleUri(requireContext(), documentUri)
            ?: return@registerForActivityResult
        val documentStream = requireContext().contentResolver.openInputStream(documentUri)
            ?: return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch {
            val text = viewModel.openDocumentExample(documentStream)
            binding.output.text = getString(R.string.saf_open_file_output, documentFile.name, text)
        }
    }

    private val actionOpenDocumentTree = registerForActivityResult(OpenDocumentTree()) { uri ->
        val documentUri = uri ?: return@registerForActivityResult
        val context = requireContext().applicationContext

        val parentFolder = DocumentFile.fromTreeUri(context, documentUri)
            ?: return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch {
            val text = viewModel.listFiles(parentFolder)
                .sortedBy { it.first }
                .joinToString { it.first }
            binding.output.text = getString(R.string.saf_folder_output, text)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafBinding.inflate(inflater, container, false)

        binding.createFile.setOnClickListener {
            actionCreateDocument.launch(DEFAULT_FILE_NAME)
        }
        binding.openFile.setOnClickListener {
            actionOpenDocument.launch(arrayOf("*/*"))
        }
        binding.openFolder.setOnClickListener {
            actionOpenDocumentTree.launch(null)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}