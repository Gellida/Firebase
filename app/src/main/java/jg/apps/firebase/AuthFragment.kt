package jg.apps.firebase


import android.app.AlertDialog
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import jg.apps.firebase.databinding.FragmentAuthBinding


class AuthFragment : Fragment(R.layout.fragment_auth) {
    private lateinit var binding: FragmentAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val callbackManager = CallbackManager.Factory.create()

    companion object {
        const val GOOGLE_SIGN_IN = 100
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAuthBinding.inflate(inflater, container, false)

        //Evento de analitics
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        val bundle = Bundle()
        bundle.putString("message", "Integracion de firebase completa")
        analytics.logEvent("InitScreen", bundle)

        setup()
        session()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        view?.isVisible = true
    }

    private fun session() {
        val context: Context? = activity
        val prefs = context!!.getSharedPreferences(
            getString(R.string.prefs_file), Context.MODE_PRIVATE
        )
        val email: String? = prefs.getString("email", null)
        val provider: String? = prefs.getString("provider", null)

        if (email != null && provider != null) {

            view?.isVisible = false
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup() {

        auth = Firebase.auth
        findNavController().enableOnBackPressed(true)


        binding.btnSignUp.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty()
                && binding.passwordEditText.text.isNotEmpty()
            ) {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        binding.emailEditText.text.toString(),
                        binding.passwordEditText.text.toString()
                    ).addOnCompleteListener {

                        if (it.isSuccessful) {
                            showHome(it.result.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }

                    }
            }
        }

        binding.btnLogIn.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty()
                && binding.passwordEditText.text.isNotEmpty()
            ) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(
                        binding.emailEditText.text.toString(),
                        binding.passwordEditText.text.toString()
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            showHome(it.result.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        binding.btnSignUpWithGoogle.setOnClickListener {

            //configuracion
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(requireActivity(), googleConf)

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
            googleClient.signOut()

        }

        binding.btnSignUpWithFacebook.setOnClickListener {

            LoginManager.getInstance().logIn(this,listOf("email"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {

                    override fun onCancel() {

                    }

                    override fun onError(error: FacebookException) {
                        showAlert()
                    }

                    override fun onSuccess(result: LoginResult) {
                        result.let {

                            val token = it.accessToken
                            val credential: AuthCredential = FacebookAuthProvider.getCredential(token.token)
                            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {

                                if (it.isSuccessful) {
                                    showHome(it.result.user?.email ?: "", ProviderType.FACEBOOK)
                                } else {
                                    showAlert()
                                }

                            }
                        }
                    }
                })

        }

        binding.btnForzarError.setOnClickListener {
            //Forzando un error

            throw RuntimeException("Forzado de error")
        }
    }


    private fun showAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType) {
        val action = AuthFragmentDirections.actionAuthFragmentToHomeFragment(provider, email)
        //findNavController().previousBackStackEntry?.savedStateHandle?.set(email,provider)
        findNavController().navigate(action)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode,resultCode,data)
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account = task.getResult(ApiException::class.java)!!
                showHome(account.email ?: "", ProviderType.GOOGLE)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)

                //             firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

}


