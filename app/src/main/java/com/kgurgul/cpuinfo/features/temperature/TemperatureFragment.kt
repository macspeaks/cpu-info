/*
 * Copyright 2017 KG Soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kgurgul.cpuinfo.features.temperature

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kgurgul.cpuinfo.R
import com.kgurgul.cpuinfo.databinding.FragmentTemperatureBinding
import com.kgurgul.cpuinfo.di.Injectable
import com.kgurgul.cpuinfo.di.ViewModelInjectionFactory
import com.kgurgul.cpuinfo.features.temperature.list.TemperatureAdapter
import com.kgurgul.cpuinfo.features.temperature.list.TemperatureItem
import com.kgurgul.cpuinfo.utils.AutoClearedValue
import timber.log.Timber
import javax.inject.Inject

/**
 * Displays information about available temperatures. Remove activity wrappers when Lifecycle
 * components will be integrated with support library.
 *
 * @author kgurgul
 */
class TemperatureFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelInjectionFactory: ViewModelInjectionFactory<TemperatureViewModel>

    @Inject
    lateinit var temperatureAdapter: AutoClearedValue<TemperatureAdapter>

    private lateinit var viewModel: TemperatureViewModel
    private lateinit var binding: AutoClearedValue<FragmentTemperatureBinding>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelInjectionFactory)
                .get(TemperatureViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = AutoClearedValue(this,
                DataBindingUtil.inflate(inflater, R.layout.fragment_temperature, container, false))
        binding.get().viewModel = viewModel
        setupRecycleView()
        return binding.value?.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.startTemperatureRefreshing()
    }

    override fun onStop() {
        viewModel.stopTemperatureRefreshing()
        super.onStop()
    }

    /**
     * Set all necessary data for [android.support.v7.widget.RecyclerView]
     */
    private fun setupRecycleView() {
        val layoutManager = LinearLayoutManager(context)
        binding.get().apply {
            tempRv.layoutManager = layoutManager
            tempRv.adapter = temperatureAdapter.get()
            (tempRv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }

        viewModel.temperatureItemsLiveData.observe(this, Observer<List<TemperatureItem>> { tempList ->
            Timber.i("temperatureItemsLiveData observer refreshed")
            temperatureAdapter.get().setTempItems(tempList)
        })
    }

    /**
     * Temporary fix for https://issuetracker.google.com/issues/74139250
     */
    override fun onDestroy() {
        val activity = activity
        if (activity != null
                && activity.isFinishing
                && !activity.isChangingConfigurations) {
            viewModelStore.clear()
        }
        super.onDestroy()
    }
}
