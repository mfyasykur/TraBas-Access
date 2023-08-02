package com.ppb.trabas_access.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.github.chrisbanes.photoview.PhotoView
import com.ppb.trabas_access.R

class ImageViewerDialog : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_RESOURCE = "image_resource"

        fun newInstance(imageResource: Int): ImageViewerDialog {
            val args = Bundle()
            args.putInt(ARG_IMAGE_RESOURCE, imageResource)
            val fragment = ImageViewerDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private var imageResource: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ImageViewerDialogStyle)
        imageResource = arguments?.getInt(ARG_IMAGE_RESOURCE) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_image_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoView = view.findViewById<PhotoView>(R.id.photoView)
        photoView.setImageResource(imageResource)
    }

}