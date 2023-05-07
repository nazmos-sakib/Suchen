package com.example.suchen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;

import com.example.suchen.databinding.ActivityMainBinding;
import com.example.suchen.databinding.ActivityPermissionDeniedBinding;

public class PermissionDeniedActivity extends AppCompatActivity {
    private ActivityPermissionDeniedBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionDeniedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_permission_denied);

        new AlertDialog.Builder(this)
                .setMessage("You have permanently denied this permission, go to settings to enable this permission")
                .setCancelable(false)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gotoApplicationSettings();
                    }
                })
                .setNegativeButton("Cancel",null)
                .setCancelable(false)
                .show();

        SpannableString ss = new SpannableString( getResources().getString(R.string.permissionDeniedText));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                gotoApplicationSettings();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, getResources().getString(R.string.permissionDeniedText).length()-8, getResources().getString(R.string.permissionDeniedText).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.textView.setText(ss);
        binding.textView.setMovementMethod(LinkMovementMethod.getInstance());
        binding.textView.setHighlightColor(Color.TRANSPARENT);
    }

    private void gotoApplicationSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}