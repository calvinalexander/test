package com.clinicapp.ui.glo.dialog

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import com.clinicapp.R
import com.clinicapp.databinding.DialogSheetChoiceBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SheetChoice : BottomSheetDialogFragment() {
    private lateinit var _binding: DialogSheetChoiceBinding
    private val binding get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DialogSheetChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var position = arrayOf("Forehead", "Below Eye", "Upper Cheek", "Lower Cheek", "Chin")
        var ex_position = arrayOf("Select Position")
        var sick = arrayOf("Normal", "Red Dot", "Pegmentation")
        binding.spinnerPosition?.adapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            position
        ) as SpinnerAdapter
        binding.spinnerPosition?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //..
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item = parent?.getItemAtPosition(position).toString()
                    Log.e("item", " "+item+position)
                    when (position) {
                        0 -> {
                            Log.e("item2", " "+item+position)
                            ex_position =
                                arrayOf("Forehead Left", "Forehead Center", "Forehead Right")
                        }
                        1 -> ex_position = arrayOf("Below Eye Left", "Below Eye Right")
                        2 -> ex_position = arrayOf("Upper Cheek Left", "Upper Cheek Right")
                        3 -> ex_position = arrayOf("Lower Cheek Left", "Lower Cheek Right")
                        4 -> ex_position = arrayOf("Chin")
                    }
                    binding.spinnerExactPosition?.adapter = ArrayAdapter(
                        requireActivity(),
                        R.layout.support_simple_spinner_dropdown_item,
                        ex_position
                    ) as SpinnerAdapter
                    binding.spinnerExactPosition?.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                //..
                            }

                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val item = parent?.getItemAtPosition(position).toString()
                                //..
                            }

                        }
                    //..
                }

            }
        binding.spinnerSick?.adapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            sick
        ) as SpinnerAdapter
        binding.spinnerSick?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //..
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item = parent?.getItemAtPosition(position).toString()
                    //..
                }

            }

        binding.btnSubmit.setOnClickListener { dismiss() }
    }

}