package vigi.patient.view.utils.editText;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import vigi.patient.R;


import static com.google.common.base.Preconditions.checkNotNull;

public class ClearableLockableEditText extends AppCompatEditText implements View.OnTouchListener{

	final Drawable imgX = getResources().getDrawable(R.drawable.ic_cancel_black_24dp); // X image

	public ClearableLockableEditText(Context context) {
		super(context);
		init();
	}

	public ClearableLockableEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ClearableLockableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {

		// Set bounds of our X button

		imgX.setBounds(0, 0, imgX.getIntrinsicWidth(), imgX.getIntrinsicHeight());

		// There may be initial text in the field, so we may need to display the button

		manageClearButton();

		setOnTouchListener(this);

		this.addTextChangedListener(generalTextWatcher);

	}

	// Implement generalOnTouchListener


	@Override
	public boolean onTouch(View v, MotionEvent event) {

			ClearableLockableEditText et = ClearableLockableEditText.this;

			// Is there an X showing?
			if (et.getCompoundDrawables()[2] == null) return false;
			// Only do this for up touches
			if (event.getAction() != MotionEvent.ACTION_UP) return false;
			// Is touch on our clear button?
			if (event.getX() > et.getWidth() - et.getPaddingRight() - imgX.getIntrinsicWidth()) {
				et.setText("");
				ClearableLockableEditText.this.removeClearButton();
			}
			return false;
	}

	// Implement TextWatcher

	private TextWatcher generalTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

			ClearableLockableEditText.this.manageClearButton();
		}

		@Override
		public void afterTextChanged(Editable arg0) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
	};

	void manageClearButton() {
		checkNotNull(this.getText());
		if (this.getText().toString().equals("") )
			removeClearButton();
		else
			addClearButton();
	}
	void addClearButton() {
		this.setCompoundDrawables(this.getCompoundDrawables()[0],
				this.getCompoundDrawables()[1],
				imgX,
				this.getCompoundDrawables()[3]);

	}
	void removeClearButton() {
		this.setCompoundDrawables(this.getCompoundDrawables()[0],
				this.getCompoundDrawables()[1],
				null,
				this.getCompoundDrawables()[3]);
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		return keyCode == KeyEvent.KEYCODE_BACK;
	}

}