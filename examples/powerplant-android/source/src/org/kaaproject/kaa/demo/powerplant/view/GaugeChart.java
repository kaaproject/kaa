package org.kaaproject.kaa.demo.powerplant.view;

import org.kaaproject.kaa.demo.powerplant.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GaugeChart extends View {
	// tag for logging
	private static final String TAG = "GaugeChartView";
	
	// constant declarations
	private static final float EPS = 1e-3f;
	private static final float ZERO = 0.0f;
	private static final long MINUS_ONE = -1L;
	private static final float CENTER_X = 0.5f;
	private static final float CENTER_Y = 0.5f;
	
	// background
	private static final int PREFFERED_SIZE = 150;
	private Bitmap background; // holds the cached static part
	private Paint backgroundPaint;
	
	// rim
	private static final int RIM_COLOR = Color.rgb(224, 224, 224);
	private static final float RIM_SIZE = 0.02f;
	private RectF rimRect;
	private Paint rimPaint;
	private Paint rimCirclePaint;
	
	// face
	private RectF faceRect;
	private Paint facePaint;
	
	// scale
	private static final float SCALE_LINE_POSITION = 0.1f;
	private static final float SCALE_LINE_WIDTH = 0.062f;
	private static final float SCALE_FONT_SIZE = 0.07f;
	private static final int TOTAL_NICKS = 7;
	private static final float DEGREES_PER_NICK = 180.0f / (TOTAL_NICKS - 1);	
	private static final int CENTER_POWER = 3;
	private static final int MIN_POWER = 0;
	private static final int MAX_POWER = 6;
	private static final int GRADIET_START_COLOR = Color.rgb(4, 221, 132);
	private RectF scaleRect;
	private Paint scalePaint;
	private RectF scaleLineRect;
	private Paint scaleLinePaint;
	private Paint redPaint;
	private Paint gradientStartPaint;

	// labels and text
	private static final int TEXT_COLOR = Color.rgb(98, 98, 98);
	private static final float TEXT_FONT_SIZE = 0.07f;
	private static final float TEXT_MEASURE_FONT_SIZE = 0.12f;
	private static final String POWER_LABEL = "Power, kW";
	private static final float POWER_LABEL_POS_Y = 0.45f;
	private static final float PANEL_NAME_LABEL_POS_Y = 0.75f;
	private static final float MEASURES_LABEL_POS_Y = 0.65f;
	private String panelName;
	private Paint textPaint;
	private TextPaint labelPaint;
	private TextPaint measuresPaint;

	// hand
	private static final float HAND_CIRCLE_RADIUS = 0.01f;
	private static final int HAND_COLOR = TEXT_COLOR;
	private Paint handPaint;
	private Path handPath;
	private Paint handScrewPaint;

	// physics
	private float handleAccelerationCoef = 3f;
	private boolean isHandInitialized = false;
	private float handTarget = CENTER_POWER;
	private float handPosition = MIN_POWER;
	private float prevHandTarget = handTarget;
	private float handVelocity = 0.0f;
	private float handAcceleration = 0.0f;
	private long lastHandMoveTime = -1L;
	private float prevDistance = 1000f;
	
	public GaugeChart(Context context) {
		super(context);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		init();
	}

	public GaugeChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(
			attrs,
			R.styleable.GaugeChart,
			0, 0);

		try {
			panelName = a.getString(R.styleable.GaugeChart_panel_name);
			handleAccelerationCoef = a.getFloat(R.styleable.GaugeChart_update_time_s, 1.0f);
			handleAccelerationCoef *= 1.2;
		} finally {
			a.recycle();
		}
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		init();
		Log.d(TAG, panelName + " has initialized");
	}

	public GaugeChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		init();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {	
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		
		setMeasuredDimension(chosenDimension, chosenDimension);
	}
	
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else {
			return PREFFERED_SIZE;
		} 
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		regenerateBackground();
	}
	
	private void regenerateBackground() {
		// free the old bitmap
		if (background != null) {
			background.recycle();
		}
		
		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		
		
		Paint paint = new Paint(); 
		paint.setColor(Color.WHITE); 
		paint.setStyle(Style.FILL); 
		backgroundCanvas.drawPaint(paint); 
		
		
		float scale = (float) getWidth();		
		backgroundCanvas.scale(scale, scale);
		
		drawRim(backgroundCanvas);
		drawFace(backgroundCanvas);
		drawScale(backgroundCanvas);
		drawLabels(backgroundCanvas);
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		Parcelable superState = bundle.getParcelable("superState");
		super.onRestoreInstanceState(superState);
		
		isHandInitialized = bundle.getBoolean("isHandInitialized");
		handPosition = bundle.getFloat("handPosition");
		handTarget = bundle.getFloat("handTarget");
		prevHandTarget = bundle.getFloat("prevHandTarget");
		handVelocity = bundle.getFloat("handVelocity");
		handAcceleration = bundle.getFloat("handAcceleration");
		lastHandMoveTime = bundle.getLong("lastHandMoveTime");
		panelName = bundle.getString("panelName");
		handleAccelerationCoef = bundle.getFloat("handleAccelerationCoef");
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		
		Bundle state = new Bundle();
		state.putParcelable("superState", superState);
		state.putBoolean("isHandInitialized", isHandInitialized);
		state.putFloat("handPosition", handPosition);
		state.putFloat("handTarget", handTarget);
		state.putFloat("prevHandTarget", prevHandTarget);
		state.putFloat("handVelocity", handVelocity);
		state.putFloat("handAcceleration", handAcceleration);
		state.putLong("lastHandMoveTime", lastHandMoveTime);
		state.putString("panelName", panelName);
		state.putFloat("handleAccelerationCoef", handleAccelerationCoef);
		return state;
	}
	
	public void setValue(double voltage) {
		setHandTarget(voltage);
	}
	
	private void init() {	
		initDrawingTools();
		setValue(0);
	}
	
	private void initDrawingTools() {	
		// background
		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
		backgroundPaint.setColor(Color.WHITE);

		// rim
		rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);
		
		rimPaint = new Paint();
		rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		rimPaint.setColor(RIM_COLOR);

		rimCirclePaint = new Paint();
		rimCirclePaint.setAntiAlias(true);
		rimCirclePaint.setStyle(Paint.Style.STROKE);
		rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
		rimCirclePaint.setStrokeWidth(0.005f);
		
		// face
		faceRect = new RectF();
		faceRect.set(rimRect.left + RIM_SIZE, rimRect.top + RIM_SIZE, 
			     rimRect.right - RIM_SIZE, rimRect.bottom - RIM_SIZE);	
		
		facePaint = new Paint();
		facePaint.setColor(Color.WHITE);
		facePaint.setFilterBitmap(true);
		facePaint.setStyle(Paint.Style.FILL);
		
		// scale
		scaleRect = new RectF();
		scaleRect.set(faceRect.left + SCALE_LINE_POSITION, faceRect.top + SCALE_LINE_POSITION,
					  faceRect.right - SCALE_LINE_POSITION, faceRect.bottom - SCALE_LINE_POSITION);
		
		scalePaint = new Paint();
		scalePaint.setStyle(Paint.Style.STROKE);
		scalePaint.setColor(Color.WHITE);
		scalePaint.setStrokeWidth(0.01f);
		scalePaint.setAntiAlias(true);
		
		scalePaint.setTextSize(0.045f);
		scalePaint.setTypeface(Typeface.SANS_SERIF);
		scalePaint.setTextScaleX(0.8f);
		scalePaint.setTextAlign(Paint.Align.CENTER);
		
		scaleLineRect = new RectF();
		scaleLineRect.set(faceRect.left + SCALE_LINE_POSITION, faceRect.top + SCALE_LINE_POSITION,
				  faceRect.right - SCALE_LINE_POSITION, faceRect.bottom - SCALE_LINE_POSITION);
		
		scaleLinePaint = new Paint();
		scaleLinePaint.setStyle(Paint.Style.STROKE);
		Shader gradient = new SweepGradient(scaleLineRect.centerX(), scaleLineRect.centerY(),
				GRADIET_START_COLOR, Color.BLACK);
		scaleLinePaint.setShader(gradient);
		scaleLinePaint.setStrokeWidth(SCALE_LINE_WIDTH);
		scaleLinePaint.setAntiAlias(true);
		
		gradientStartPaint = new Paint();
		gradientStartPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		gradientStartPaint.setColor(GRADIET_START_COLOR);
		
		
		redPaint = new Paint();
		redPaint.setColor(Color.RED);
		redPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		redPaint.setStrokeWidth(SCALE_LINE_WIDTH * 0.8f);
		redPaint.setAntiAlias(true);
		
		// paint for panel name label and power label
		textPaint = new Paint();
		textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint.setColor(TEXT_COLOR);
		textPaint.setTextSize(SCALE_FONT_SIZE);
		textPaint.setTextAlign(Paint.Align.LEFT);
		textPaint.setTypeface(Typeface.SANS_SERIF);
		textPaint.setAntiAlias(true);
		
		// hand 
		handPaint = new Paint();
		handPaint.setAntiAlias(true);
		handPaint.setColor(HAND_COLOR);		
		handPaint.setStyle(Paint.Style.FILL);	
		handPaint.setPathEffect(null);
		
		handPath = new Path();
		handPath.moveTo(CENTER_X, CENTER_Y + 0.05f);
		handPath.lineTo(CENTER_X - 0.010f, CENTER_Y + 0.05f - 0.007f);
		handPath.lineTo(CENTER_X - 0.002f, CENTER_Y - 0.32f);
		handPath.lineTo(CENTER_X + 0.002f, CENTER_Y - 0.32f);
		handPath.lineTo(CENTER_X + 0.010f, CENTER_Y + 0.05f - 0.007f);
		handPath.lineTo(CENTER_X, CENTER_Y + 0.05f);
		handPath.addCircle(CENTER_X, CENTER_Y, 0.02f, Path.Direction.CW);
		handPath.close();
		
		handScrewPaint = new Paint();
		handScrewPaint.setAntiAlias(true);
		handScrewPaint.setColor(0xff493f3c);
		handScrewPaint.setStyle(Paint.Style.FILL);
		
		labelPaint = new TextPaint();
		labelPaint.setAntiAlias(true);
		labelPaint.setColor(TEXT_COLOR);
		labelPaint.setTextSize(TEXT_FONT_SIZE);
		labelPaint.setTypeface(Typeface.SANS_SERIF);
		labelPaint.setTextAlign(Paint.Align.CENTER);
		labelPaint.setLinearText(true);
		
		// measures (x.y kW)
		measuresPaint = new TextPaint();
		measuresPaint.setColor(TEXT_COLOR);
		measuresPaint.setAntiAlias(true);
		measuresPaint.setStyle(Style.FILL);
		measuresPaint.setTextSize(TEXT_MEASURE_FONT_SIZE);
		measuresPaint.setTypeface(Typeface.SANS_SERIF);
		measuresPaint.setTextAlign(Paint.Align.CENTER);
		measuresPaint.setLinearText(true);
	}
	
	private void drawRim(Canvas canvas) {
		// first, draw the metallic body
		canvas.drawOval(rimRect, rimPaint);
		// now the outer rim circle
		canvas.drawOval(rimRect, rimCirclePaint);
	}
	
	private void drawFace(Canvas canvas) {		
		canvas.drawOval(faceRect, facePaint);
		// draw the inner rim circle
		canvas.drawOval(faceRect, rimCirclePaint);
	}
	
	private void drawLabels(Canvas canvas) {
		Log.d(TAG, labelPaint.measureText(POWER_LABEL) + "");
//		canvas.drawText(POWER_LABEL, CENTER_X - Layout.getDesiredWidth(POWER_LABEL, labelPaint) / 2,
		canvas.drawText(POWER_LABEL, CENTER_X, POWER_LABEL_POS_Y, labelPaint);
		canvas.drawText(panelName, CENTER_X, PANEL_NAME_LABEL_POS_Y, labelPaint);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		drawBackground(canvas);

		float scale = (float) getWidth();		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);
		
		drawHand(canvas);
		drawMeasures(canvas);
		
		canvas.restore();
	
		if (handNeedsToMove()) {
			moveHand();
		}
	}
	
	private void drawBackground(Canvas canvas) {
		if (background == null) {
			Log.w(TAG, "Background not created");
		} else {
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}
	
	private void drawScale(Canvas canvas) {
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(180, CENTER_X, CENTER_Y);
		canvas.drawArc(scaleRect, -6, 188, false, scaleLinePaint);
		canvas.restore();
		
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.rotate(-90, CENTER_X, CENTER_Y);
		for (int i = 0; i < TOTAL_NICKS * 2 - 1; ++i) {
			float y1 = scaleRect.top;
			float y2 = scaleRect.top + SCALE_LINE_WIDTH / 2;
			float dSize = SCALE_LINE_WIDTH / 4;
			
			if (i % 2 == 0) {
				canvas.drawLine(CENTER_X, y1 - dSize, CENTER_X, y2, scalePaint);
				String valueString = String.valueOf(i / 2);
				canvas.drawText(valueString, CENTER_X - 0.02f, y1 - SCALE_LINE_WIDTH * 0.6f, textPaint);
			} else {
				canvas.drawLine(CENTER_X, y1, CENTER_X, y2, scalePaint);
			}
			
			canvas.rotate(DEGREES_PER_NICK / 2, CENTER_X, CENTER_Y);
		}
		canvas.restore();
		canvas.drawArc(scaleLineRect, 174, 5, false, redPaint);
	}
	
	private void drawHand(Canvas canvas) {
		if (isHandInitialized) {
			float handAngle = degreeToAngle(handPosition);
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate(handAngle, CENTER_X, CENTER_Y);
			canvas.drawPath(handPath, handPaint);
			canvas.restore();
			
			canvas.drawCircle(CENTER_X, CENTER_Y, HAND_CIRCLE_RADIUS, handScrewPaint);
		}
	}
	
	@SuppressLint("DefaultLocale")
	private void drawMeasures(Canvas canvas) {
		if (isHandInitialized) {
			String measures = String.format("%2.1f kW", handPosition);
			canvas.drawText(measures, CENTER_X, MEASURES_LABEL_POS_Y, measuresPaint);
		}
	}
	
	private void moveHand() {
		if (! handNeedsToMove()) {
			return;
		}
		
		if (lastHandMoveTime != MINUS_ONE) { 
			long currentTime = System.currentTimeMillis();
			float delta = (currentTime - lastHandMoveTime) / 1000.0f;

			// previous (from previous rotation cycle) hand target and current target are not the same
			if (Math.abs(handTarget - prevHandTarget) > EPS) {
				handVelocity = ZERO;
				prevHandTarget = handTarget;
			}
			
			handPosition += handVelocity * delta + handAcceleration * delta * delta / 2;
			handVelocity += handAcceleration * delta;
			
			float curDistance = Math.abs(handTarget - handPosition);
			if (curDistance >= prevDistance || curDistance < EPS) {
				resetHand();
			} else {
				lastHandMoveTime = System.currentTimeMillis();				
			}
			prevDistance = curDistance;
			
			// prevent hand moving out of possible bounds
			if (handPosition < MIN_POWER) {
				handPosition = MIN_POWER;
				resetHand();
			} else if (handPosition > MAX_POWER) {
				handPosition = MAX_POWER;
				resetHand();
			}
			
			invalidate();
		} else {
			lastHandMoveTime = System.currentTimeMillis();
			moveHand();
		}
	}
	
	private void setHandTarget(double power) {
		if (power < MIN_POWER) {
			power = MIN_POWER;
		} else if (power > MAX_POWER) {
			power = MAX_POWER;
		}
		handTarget = (float) power;
		prevDistance = 10000f;
		isHandInitialized = true;
		handAcceleration = 2 * (handTarget - handPosition) / (handleAccelerationCoef * handleAccelerationCoef);
		invalidate();
	}
	
	private float degreeToAngle(float degree) {
		return (degree - CENTER_POWER) * DEGREES_PER_NICK;
	}
	
	private boolean handNeedsToMove() {
		return Math.abs(handPosition - handTarget) > EPS;
	}
	
	private void resetHand() {
		handVelocity = ZERO;
		handAcceleration = ZERO;
		lastHandMoveTime = MINUS_ONE;
	}
}
