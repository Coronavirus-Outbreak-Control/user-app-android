package com.example.coronavirusherdimmunity.introduction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coronavirusherdimmunity.R;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class PhoneRegistrationActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_registration);

        mContext = this;

        final LinearLayout registrationLayout = findViewById(R.id.phone_layout);
        final LinearLayout verificationLayout = findViewById(R.id.verification_layout);
        final EditText edtPhone = findViewById(R.id.phone_number_edt);
        final EditText edtConfirm = findViewById(R.id.verification_code_edt);

        Button btnRegister = findViewById(R.id.button_register);
        Button btnConfirm = findViewById(R.id.button_confirm);
        final TextView btnEditPhone = findViewById(R.id.edit_phone_btn);

        final CountryCodePicker ccp = findViewById(R.id.ccp);
        ccp.registerPhoneNumberTextView(edtPhone);

        verificationLayout.setVisibility(View.GONE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ccp.isValid()) {
                    Toast.makeText(mContext, "number " + ccp.getFullNumber() + " is valid.", Toast.LENGTH_LONG).show();

                    // TODO: 22/03/2020 register number to backend

                    registrationLayout.setVisibility(View.GONE);
                    verificationLayout.setVisibility(View.VISIBLE);
                    edtConfirm.requestFocus();
                } else {
                    Toast.makeText(mContext, "number " + ccp.getFullNumber() + " not valid!!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 22/03/2020 VERIFY CODE
                if(edtConfirm.getText().length()>0) {
                    Toast.makeText(mContext, "code " + edtConfirm.getText() + " is valid.", Toast.LENGTH_LONG).show();
                    // TODO: 22/03/2020 GO NEXT
                } else {
                    Toast.makeText(mContext, "code " + edtConfirm.getText() + " not valid!!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrationLayout.setVisibility(View.VISIBLE);
                verificationLayout.setVisibility(View.GONE);
                edtPhone.requestFocus();
            }
        });
    }
}
