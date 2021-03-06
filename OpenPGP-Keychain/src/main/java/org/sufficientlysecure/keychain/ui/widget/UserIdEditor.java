/*
 * Copyright (C) 2010-2014 Thialfihar <thi@thialfihar.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thialfihar.android.apg.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.beardedhen.androidbootstrap.BootstrapButton;

import org.thialfihar.android.apg.R;
import org.thialfihar.android.apg.helper.ContactHelper;
import org.thialfihar.android.apg.pgp.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserIdEditor extends LinearLayout implements Editor, OnClickListener {
    private EditorListener mEditorListener = null;

    private BootstrapButton mDeleteButton;
    private EditText mName;
    private AutoCompleteTextView mEmail;
    private EditText mComment;

    // see http://www.regular-expressions.info/email.html
    // RFC 2822 if we omit the syntax using double quotes and square brackets
    // android.util.Patterns.EMAIL_ADDRESS is only available as of Android 2.2+
    private static final Pattern EMAIL_PATTERN = Pattern
        .compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                    "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
                 Pattern.CASE_INSENSITIVE);

    public static class NoNameException extends Exception {
        static final long serialVersionUID = 0xf812773343L;

        public NoNameException(String message) {
            super(message);
        }
    }

    public void setCanBeEdited(boolean canBeEdited) {
        if (!canBeEdited) {
            mDeleteButton.setVisibility(View.INVISIBLE);
            mName.setEnabled(false);
            mEmail.setEnabled(false);
            mComment.setEnabled(false);
        }
    }

    public static class InvalidEmailException extends Exception {
        static final long serialVersionUID = 0xf812773345L;

        public InvalidEmailException(String message) {
            super(message);
        }
    }

    public UserIdEditor(Context context) {
        super(context);
    }

    public UserIdEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mDeleteButton = (BootstrapButton) findViewById(R.id.delete);
        mDeleteButton.setOnClickListener(this);

        mName = (EditText) findViewById(R.id.name);
        mEmail = (AutoCompleteTextView) findViewById(R.id.email);
        mComment = (EditText) findViewById(R.id.comment);


        mEmail.setThreshold(1); // Start working from first character
        mEmail.setAdapter(
                new ArrayAdapter<String>
                        (this.getContext(), android.R.layout.simple_dropdown_item_1line,
                                                ContactHelper.getMailAccounts(getContext())
                        ));
        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String email = editable.toString();
                if (email.length() > 0) {
                    Matcher emailMatcher = Patterns.EMAIL_ADDRESS.matcher(email);
                    if (emailMatcher.matches()) {
                        mEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    android.R.drawable.presence_online, 0);
                    } else {
                        mEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                    android.R.drawable.presence_offline, 0);
                    }
                } else {
                    // remove drawable if email is empty
                    mEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        });

        super.onFinishInflate();
    }

    public void setValue(String userId) {
        mName.setText("");
        mComment.setText("");
        mEmail.setText("");

        String[] chunks = Utils.splitUserId(userId);
        if (chunks[0] != null) {
            mName.setText(chunks[0]);
            mEmail.setText(chunks[1]);
            mComment.setText(chunks[2]);
        }
    }

    public String getValue() throws NoNameException {
        String name = ("" + mName.getText()).trim();
        String email = ("" + mEmail.getText()).trim();
        String comment = ("" + mComment.getText()).trim();

        String userId = name;
        if (comment.length() > 0) {
            userId += " (" + comment + ")";
        }
        if (email.length() > 0) {
            userId += " <" + email + ">";
        }

        if (userId.equals("")) {
            // ok, empty one...
            return userId;
        }

        // otherwise make sure that name and email exist
        if (name.equals("")) {
            throw new NoNameException("need a name");
        }

        return userId;
    }

    public void onClick(View v) {
        final ViewGroup parent = (ViewGroup) getParent();
        if (v == mDeleteButton) {
            parent.removeView(this);
            if (mEditorListener != null) {
                mEditorListener.onDeleted(this);
            }
        }
    }

    public void setEditorListener(EditorListener listener) {
        mEditorListener = listener;
    }
}
