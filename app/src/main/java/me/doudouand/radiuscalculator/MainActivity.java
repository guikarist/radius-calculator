package me.doudouand.radiuscalculator;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "me.doudouand.radiuscalculator.MESSAGE";
    private static final String STATE_RESULT = "calculateResult";

    private TextInputEditText mArchInput;
    private TextInputEditText mChordInput;
    private double mCurrentResult;
    private Button mButton;

    private boolean isEmpty(TextInputEditText v) {
        if (v.getText() == null) {
            throw new NullPointerException();
        }
        return v.getText().toString().trim().length() == 0;
    }

    private boolean isZero(TextInputEditText v) {
        if (!isEmpty(v)) {
            if (v.getText() == null) {
                throw new NullPointerException();
            }

            return Double.parseDouble(v.getText().toString()) == 0;
        }

        return false;
    }

    private boolean isTooBig(TextInputEditText v) {
        if (!isEmpty(v)) {
            if (v.getText() == null) {
                throw new NullPointerException("In isTooBig() function");
            }

            String input = v.getText().toString();

            int index = input.indexOf(".");
            if (index == -1) {
                index = input.length() - 1;
            }

            return (index + 1) > 14;
        }

        return false;
    }

    private boolean isTooSmall(TextInputEditText v) {
        if (!isEmpty(v)) {
            if (v.getText() == null) {
                throw new NullPointerException("In isTooSmall() function");
            }

            String input = v.getText().toString();
            if (input.startsWith("0.") && !input.substring(input.length() - 1).equals("0")
                    && input.length() > 15) {
                for (int i = 2; i <= 14; ++i) {
                    if (input.charAt(i) != '0') {
                        return false;
                    }
                }
                return true;
            }

            return false;
        }

        return false;
    }

    private boolean startWithDot(TextInputEditText v) {
        if (!isEmpty(v)) {
            if (v.getText() == null) {
                throw new NullPointerException("In startWithDot() function");
            }

            return v.getText().toString().startsWith(".");
        }

        return false;
    }

    private boolean isDouble(String v) {
        try {
            double temp = Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isIllegal(TextInputEditText v) {
        if (!isEmpty(v)) {
            if (v.getText() == null) {
                throw new NullPointerException("In isIllegal() function");
            }

            return isDouble(v.getText().toString());
        }

        return false;
    }

    private double calcRadius(double chord, double arch) {
        return arch / 2 + Math.pow(chord, 2) / (8 * arch);
    }

    private String nicelyFormatDouble(double d) {
        if (d == Math.round(d)) {
            return String.format(Locale.getDefault(), "%d", Math.round(d));
        } else if (d < Math.pow(10, 8)) {
            return String.format(Locale.getDefault(), "%s", Math.round(d * 1000) / 1000.0);
        } else {
            return String.format(Locale.getDefault(), "%.3e", d);
        }
    }

    private void setError(TextInputEditText v, String str) {
        TextInputLayout parent = (TextInputLayout) v.getParent().getParent();
        parent.setError(str);
    }

    private void clearError(TextInputEditText v) {
        TextInputLayout parent = (TextInputLayout) v.getParent().getParent();
        parent.setError(null);
    }

    private boolean hasError(TextInputEditText v) {
        TextInputLayout parent = (TextInputLayout) v.getParent().getParent();
        return parent.getError() != null;
    }

    private void dotHandler(TextInputEditText v) {
        if (v.getText() == null) {
            throw new NullPointerException("In dotHandler() function");
        }

        String temp = "0" + v.getText().toString();
        v.setText(temp);
        v.setSelection(v.getText().length());
    }

    private void getResult() {
        if (mArchInput.getText() == null || mChordInput.getText() == null) {
            throw new NullPointerException("EditView.getTex() in getResult() function");
        } else if (isEmpty(mArchInput) || isEmpty(mChordInput)) {
            throw new NullPointerException("Empty input in getResult() function");
        }

        try {
            double archValue = Double.parseDouble(mArchInput.getText().toString());
            double chordValue = Double.parseDouble(mChordInput.getText().toString());

            if (archValue <= 0 || chordValue <= 0) {
                throw new RuntimeException("Invalid zero input");
            } else {
                mCurrentResult = calcRadius(chordValue, archValue);

                TextView resultArea = findViewById(R.id.resultArea);
                resultArea.setText(nicelyFormatDouble(mCurrentResult));
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Illegal input processed in getResult() function");
        }
    }

    // Add listener to text.

    private class PreState {
        Boolean wasZero = false;
        Boolean wasTooSmall = false;
        Boolean wasBig = false;

        void clear() {
            wasZero = false;
            wasTooSmall = false;
            wasBig = false;
        }
    }

    PreState mPreArchState = new PreState();
    PreState mPreChordState = new PreState();

    private Boolean numericValidator(TextInputEditText v, PreState s) {
        if (isTooSmall(v)) {
            if (!s.wasTooSmall) {
                setError(v, getString(R.string.input_small));
                s.wasTooSmall = true;
            }
            return false;
        } else if (isZero(v)) {
            if (!s.wasZero) {
                setError(v, getString(R.string.input_zero));
                s.wasZero = true;
            }
            return false;
        } else if (isTooBig(v)) {
            if (!s.wasBig) {
                setError(v, getString(R.string.input_big));
                s.wasBig = true;
            }
            return false;
        } else {
            if (hasError(v)) {
                clearError(v);
            }
            s.clear();
            return true;
        }
    }

    private TextWatcher mInputWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Boolean isValid;

            // If input is empty...
            isValid = !isEmpty(mArchInput) && !isEmpty(mChordInput);

            // If input starts with dot...
            if (startWithDot(mArchInput)) {
                dotHandler(mArchInput);
            }
            if (startWithDot(mChordInput)) {
                dotHandler(mChordInput);
            }

            /*
             *  Validation on numeric issues.
             *  In these cases, input is regared parsable to double.
             *  If not, throw an exception.
             */
            try {
                isValid = isValid
                        & numericValidator(mChordInput, mPreChordState)
                        & numericValidator(mArchInput, mPreArchState);
            } catch (NumberFormatException e) {
                isValid = false;

                if (isIllegal(mArchInput)) {
                    setError(mArchInput, getString(R.string.input_illegal));
                }

                if (isIllegal(mChordInput)) {
                    setError(mChordInput, getString(R.string.input_illegal));
                }
            }

            if (isValid) {
                mButton.setEnabled(true);
            } else {
                mButton.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check whether we're recreating a previously destroyed instance.
        if (savedInstanceState != null) {
            mCurrentResult = savedInstanceState.getDouble(STATE_RESULT);
        } else {
            mCurrentResult = 0.0;
        }

        Log.d(EXTRA_MESSAGE, this.getClass().toString());
        setContentView(R.layout.activity_main);

        mArchInput = findViewById(R.id.archInput);
        mChordInput = findViewById(R.id.chordInput);
        mButton = findViewById(R.id.button);

        ((TextView) findViewById(R.id.resultArea)).setText(nicelyFormatDouble(mCurrentResult));

        // Set button disabled initially.
        mButton.setEnabled(false);
        mArchInput.addTextChangedListener(mInputWatcher);
        mChordInput.addTextChangedListener(mInputWatcher);

        // Add listener to button.
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getResult();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble(STATE_RESULT, mCurrentResult);
        super.onSaveInstanceState(savedInstanceState);
    }
}
