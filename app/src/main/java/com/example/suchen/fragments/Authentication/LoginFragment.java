package com.example.suchen.fragments.Authentication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.suchen.R;
import com.example.suchen.databinding.FragmentLoginBinding;
import com.example.suchen.fragments.BookmarksFragment;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment->";
    private FragmentLoginBinding binding;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setOnTouchClickListenerToRootView();
        setRegisterTextViewFancyModification();


        binding.btnLoginLoginFragment.setOnClickListener(View->{
            String email = binding.evEmailLoginFragment.getText().toString();
            String password = binding.evPasswordLoginFragment.getText().toString();

            if (isLoginCredentialsAuthentic(email, password)) {
                //progressDialog.show();
                //check for existent user with the same email and password.
                //if found add to shared preference and move to main activity
                ParseUser.logInInBackground(email, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null){
                            Toast.makeText(getActivity().getApplicationContext(),"login successful", Toast.LENGTH_SHORT).show();

                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainer_mainActivity,new BookmarksFragment(getContext()))
                                    .commit();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    //business logic for form field validation
    // check only if the email format is valid
    // no need to check for password
    private boolean isLoginCredentialsAuthentic(String email, String password){
        Log.d(TAG, "onRegBtnClicked: register button is clicked");

        //logic for authentic email and password.
        if (email.equals("") || password.equals("")) {
            binding.tvWarningLoginFragment.setVisibility(View.VISIBLE);
            binding.tvWarningLoginFragment.setText("Please enter the password and Email");
            return false;
        } else {
            String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
            // Compile regular expression to get the pattern
            Pattern pattern = Pattern.compile(regex);
            //Create instance of matcher
            Matcher matcher = pattern.matcher(email);
            if ( matcher.matches()){ //match with regex
                //pattern matches
                //email is valid
                binding.tvWarningLoginFragment.setVisibility(View.GONE);
                return true;
            }else {
                binding.tvWarningLoginFragment.setVisibility(View.VISIBLE);
                binding.tvWarningLoginFragment.setText("Please enter a valid Email");
                return false;
            }

        }

    }


    //set keyboard hiding functionality
    //it calls another function: hideKeyboard();
    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchClickListenerToRootView(){
        binding.parentLoginFragment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
    }

    //implements the functionality of hiding keyboard upon clicking anywhere in the page
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = getActivity().getCurrentFocus();
        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }


    //adding extra feature in text view login.
    // making last only clickable.
    // making it italic and underlined
    private void setRegisterTextViewFancyModification(){
        String st = "Don't have an account? Register from here";
        SpannableString spannableString = new SpannableString(st);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer_mainActivity,new SignupFragment())
                        .commit();
            }
        };
        int start = st.length()-4; // Start index of the word "clickable"
        int end = st.length();   // End index of the word "clickable"
        spannableString.setSpan(clickableSpan, start,  end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //italic text
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //underline text
        spannableString.setSpan(new UnderlineSpan(),  start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //so that the color of the text remain the same
        spannableString.setSpan(new ForegroundColorSpan(binding.tvRegisterLoginFragment.getCurrentTextColor()), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvRegisterLoginFragment.setText(spannableString);
        binding.tvRegisterLoginFragment.setMovementMethod(LinkMovementMethod.getInstance());
    }
}