package com.ppb.trabas_access.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ppb.trabas_access.databinding.CarouselItemBinding

class CarouselFragment : Fragment() {

    private lateinit var binding: CarouselItemBinding

    companion object {
        private const val ARG_IMAGE_URL = "image"

        fun newInstance(imageUrl: String?): CarouselFragment {

            val args = Bundle()
            args.putString(ARG_IMAGE_URL, imageUrl)
            val fragment = CarouselFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CarouselItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = arguments?.getString(ARG_IMAGE_URL)
        if (!imageUrl.isNullOrEmpty()) {

            Glide.with(this)
                .load(imageUrl)
                .into(binding.imageCarousel)
        }
    }
}