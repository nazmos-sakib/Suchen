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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.suchen.R;
import com.example.suchen.databinding.FragmentSignupBinding;


public class SignupFragment extends Fragment {
    private FragmentSignupBinding binding;

    Fragment returnFragment;

    public SignupFragment(Fragment returnFragment) {
        // Required empty public constructor
        this.returnFragment = returnFragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setOnTouchClickListenerToRootView();
        setRegisterTextViewFancyModification();

    }


    //set keyboard hiding functionality
    //it calls another function: hideKeyboard();
    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchClickListenerToRootView(){
        binding.parentSignupFragment.setOnTouchListener(new View.OnTouchListener() {
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
        String st = "Have an account? Login from here";
        SpannableString spannableString = new SpannableString(st);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer_mainActivity,new LoginFragment(returnFragment))
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
        spannableString.setSpan(new ForegroundColorSpan(binding.tvLoginSignupFragment.getCurrentTextColor()), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvLoginSignupFragment.setText(spannableString);
        binding.tvLoginSignupFragment.setMovementMethod(LinkMovementMethod.getInstance());
    }
}