package com.ppb.trabas_access.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import com.ppb.trabas_access.R
import com.ppb.trabas_access.databinding.FragmentRouteBinding

class RouteFragment : Fragment() {

    private lateinit var binding: FragmentRouteBinding
    private lateinit var mainRouteView: PhotoView
    private lateinit var koridor1View: PhotoView
    private lateinit var koridor2View: PhotoView
    private lateinit var koridor3View: PhotoView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainRouteView = binding.mainRoute
        koridor1View = binding.koridor1
        koridor2View = binding.koridor2
        koridor3View = binding.koridor3

        mainRouteView.setImageResource(R.drawable.map_trabas)
        koridor1View.setImageResource(R.drawable.koridor1)
        koridor2View.setImageResource(R.drawable.koridor2)
        koridor3View.setImageResource(R.drawable.koridor3)

        mainRouteView.setOnClickListener {
            showImageViewerDialog(R.raw.map_trabas)
        }
        koridor1View.setOnClickListener {
            showImageViewerDialog(R.raw.koridor1)
        }
        koridor2View.setOnClickListener {
            showImageViewerDialog(R.raw.koridor2)
        }
        koridor3View.setOnClickListener {
            showImageViewerDialog(R.raw.koridor3)
        }
    }

    private fun showImageViewerDialog(imageResource: Int) {
        val dialog = ImageViewerDialog.newInstance(imageResource)
        dialog.show(childFragmentManager, "ImageViewerDialog")
    }
}