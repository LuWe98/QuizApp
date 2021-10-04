package com.example.quizapp.view.fragments.voiceSearch

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.DfVoiceSearchBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmMain
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DialogFragmentVoiceSearch : BindingDialogFragment<DfVoiceSearchBinding>(), RecognitionListener {

    private val viewModel: VmMain by viewModels()

    private val speechRecognizer : SpeechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(requireContext()) }

    private val speechIntent : Intent by lazy {
         Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isPermissionGranted(Manifest.permission.RECORD_AUDIO)) initSpeechRecognizer() else resultLauncher.launch(Manifest.permission.RECORD_AUDIO)

        binding.root.setOnClickListener {
            if (isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                speechRecognizer.apply {
                    stopListening()
                    startListening(speechIntent)
                }
            }
        }
    }

    private val resultLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) initSpeechRecognizer() else navigator.popBackStack()
    }

    private fun initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            showToast(getString(R.string.voiceSearchSpeechRecognizerNotAvailable))
            navigator.popBackStack()
            return
        }

        speechRecognizer.apply {
            setRecognitionListener(this@DialogFragmentVoiceSearch)
            startListening(speechIntent)
        }
    }


    override fun onReadyForSpeech(params: Bundle?) {
        binding.apply {
            searchQuery.setTextColor(getColor(R.color.black))
            searchQuery.text = getString(R.string.voiceSearchStartTalking)
            progressBar.visibility = View.GONE
        }
    }

    override fun onBeginningOfSpeech() {
        binding.apply {
            searchQuery.setTextColor(getColor(R.color.black))
            searchQuery.text = null
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.run {
            if (size != 0 && get(0).isNotEmpty()) {
                onResults(partialResults)
            }
        }
    }

    override fun onResults(results: Bundle?) {
        binding.progressBar.visibility = View.GONE
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.run {
            if(size != 0 && get(0).isNotEmpty()) {
                binding.apply {
                    voiceIcon.apply {
                        setImageDrawable(getDrawable(R.drawable.ic_check))
                        imageTintList = getColorStateList(R.color.green)
                    }
                    searchQuery.apply {
                        text = get(0)
                        setTextColor(getColorStateList(R.color.green))
                    }
                    showToast("Ist Korrekt so!")
                }
            } else {
                launchDelayed(startDelay = 500) {

                }
            }
        }
    }



    /**
     * Viable Error Codes:
     * SpeechRecognizer.ERROR_NETWORK_TIMEOUT |
     * SpeechRecognizer.ERROR_CLIENT |
     * SpeechRecognizer.ERROR_NETWORK |
     * SpeechRecognizer.ERROR_AUDIO |
     * SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS |
     * SpeechRecognizer.ERROR_SERVER |
     * SpeechRecognizer.ERROR_NO_MATCH |
     * SpeechRecognizer.ERROR_SPEECH_TIMEOUT
     */
    override fun onError(errorCode: Int) {
        binding.apply {
            progressBar.visibility = View.GONE
            searchQuery.text = getErrorText(errorCode)
            searchQuery.setTextColor(getColor(getErrorTextColor(errorCode)))
        }
    }

    private fun getErrorText(errorCode: Int) = getString(
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> {
                R.string.voiceSearchAudioError
            }
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                R.string.voiceSearchInsufficientPermission
            }
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                R.string.voiceSearchTimeout
            }
            else -> {
                R.string.voiceSearchIDidNotGetThat
            }
        }
    )

    private fun getErrorTextColor(errorCode: Int) = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO, SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
            R.color.black
        }
        else -> {
            R.color.black
        }
    }


    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onEvent(p0: Int, p1: Bundle?) {}
}