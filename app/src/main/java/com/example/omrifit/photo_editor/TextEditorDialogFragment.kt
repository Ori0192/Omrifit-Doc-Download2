package com.example.omrifit.photo_editor

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.omrifit.R

/**
 * TextEditorDialogFragment is a dialog fragment that allows users to input and edit text.
 * Users can also choose the color of the text from a color picker.
 */
class TextEditorDialogFragment : DialogFragment() {

    private lateinit var mAddTextEditText: EditText
    private lateinit var mAddTextDoneTextView: TextView
    private lateinit var mInputMethodManager: InputMethodManager
    private var mColorCode = 0
    private var mTextEditorListener: TextEditorListener? = null

    /**
     * Interface to be implemented by classes that handle the text editing completion.
     */
    interface TextEditorListener {
        fun onDone(inputText: String, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        // Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()

        mAddTextEditText = view.findViewById(R.id.add_text_edit_text)
        mInputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv)

        // Setup the color picker for text color
        val addTextColorPickerRecyclerView: RecyclerView = view.findViewById(R.id.add_text_color_picker_recycler_view)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        addTextColorPickerRecyclerView.layoutManager = layoutManager
        addTextColorPickerRecyclerView.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity)

        // This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(object :
            ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mColorCode = colorCode
                mAddTextEditText.setTextColor(colorCode)
            }
        })

        addTextColorPickerRecyclerView.adapter = colorPickerAdapter

        val arguments = requireArguments()

        mAddTextEditText.setText(arguments.getString(EXTRA_INPUT_TEXT))
        mColorCode = arguments.getInt(EXTRA_COLOR_CODE)
        mAddTextEditText.setTextColor(mColorCode)
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        // Make a callback on activity when user is done with text editing
        mAddTextDoneTextView.setOnClickListener { onClickListenerView ->
            mInputMethodManager.hideSoftInputFromWindow(onClickListenerView.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText.text.toString()
            val textEditorListener = mTextEditorListener
            if (inputText.isNotEmpty() && textEditorListener != null) {
                textEditorListener.onDone(inputText, mColorCode)
            }
        }
    }

    /**
     * Sets the listener to handle the completion of text editing.
     * @param textEditorListener The listener to handle text editing completion.
     */
    fun setOnTextEditorListener(textEditorListener: TextEditorListener) {
        mTextEditorListener = textEditorListener
    }

    companion object {
        private val TAG: String = TextEditorDialogFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        /**
         * Shows the dialog with the provided text and text color.
         * If no text or color is provided, it defaults to empty text and white color.
         * @param appCompatActivity The activity where the dialog is shown.
         * @param inputText The initial text to be displayed in the editor.
         * @param colorCode The initial color of the text.
         * @return An instance of TextEditorDialogFragment.
         */
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }
}
