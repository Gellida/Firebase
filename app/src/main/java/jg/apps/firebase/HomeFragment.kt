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
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import jg.apps.firebase.databinding.FragmentHomeBinding


class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    val args: HomeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        val email: String = args.email
        val provider = args.provider.toString()
        setup(email, provider)


        val context: Context? = activity
        val prefs = context!!.getSharedPreferences(
            getString(R.string.prefs_file), Context.MODE_PRIVATE
        ).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()


        return binding.root
    }

    private fun setup(email: String, provider: String) {
        binding.emailTextView.text = email
        binding.providerTextView.text = provider

        findNavController().enableOnBackPressed(true)

        binding.btnLogOut.setOnClickListener {

            //borrado de datos
            val context: Context? = activity
            val prefs = context!!.getSharedPreferences(
                getString(R.string.prefs_file), Context.MODE_PRIVATE
            ).edit()
            prefs.clear()
            prefs.apply()

            if (provider == ProviderType.FACEBOOK.name) {
                LoginManager.getInstance().logOut()
            }

            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_homeFragment_to_authFragment)

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_homeFragment_to_authFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)

    }
}