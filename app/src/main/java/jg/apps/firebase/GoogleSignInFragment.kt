package jg.apps.firebase

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import jg.apps.firebase.databinding.FragmentGoogleSignInBinding
import jg.apps.firebase.databinding.FragmentHomeBinding


class GoogleSignInFragment : Fragment(R.layout.fragment_google_sign_in) {
    private lateinit var binding: FragmentGoogleSignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val args: GoogleSignInFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoogleSignInBinding.inflate(inflater,container,false)

        val extraName = args.extraName
        setup(extraName)


        val context: Context? = activity
        val prefs = context!!.getSharedPreferences(
            getString(R.string.prefs_file), Context.MODE_PRIVATE
        ).edit()
        prefs.putString("extraName",extraName)
        prefs.apply()


        return binding.root
    }

    private fun setup(extraName: String){
        binding.showNameTextView.text = extraName

        findNavController().enableOnBackPressed(true)

        binding.btnLogOut.setOnClickListener {
            //borrado de datos
            val context: Context? = activity
            val prefs = context!!.getSharedPreferences(
                getString(R.string.prefs_file), Context.MODE_PRIVATE
            ).edit()
            prefs.clear()
            prefs.apply()
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_googleSignInFragment_to_authFragment)
        }


        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_googleSignInFragment_to_authFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

    }
}