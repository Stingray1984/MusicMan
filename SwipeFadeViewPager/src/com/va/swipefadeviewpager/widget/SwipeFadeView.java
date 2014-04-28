package com.va.swipefadeviewpager.widget;

import com.enrique.stackblur.StackBlurManager;
import com.va.swipefadeviewpager.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwipeFadeView extends RelativeLayout {
    private final static int FOCUS_MASK_MARGIN_DEFAULT = -1;
    private final static int FOCUS_MASK_RADIUS_DEFAULT = -1;
    private final static int FOCUS_STROKE_WIDTH_DEFAULT = 0;
    private final static int DEFAULT_COLOR = 0xFF000000;
    private final static int TITLE_TEXT_SIZE_DEFAULT = 20;
    private final static int TITLE_MESSAGE_SIZE_DEFAULT = 15;
    private final static int BACKGROUND_BLUR_RADIUS_DEFAULT = 2; 
    
    private Context mContext;
    
    private CharSequence mTitleText;
    private CharSequence mMessageText;
    
    private Drawable mFocusImage;
    private Drawable mBackgroundImage;
    private Bitmap mMaskShape;
    
    private int mTitleTextSize;
    private int mTitleTextColor;
    
    private int mMessageTextSize;
    private int mMessageTextColor;
    
    private int mFocusMaskMargin;
    private int mFocusMaskRadius;
    
    private int mBackgroundBlurRadius;
    
    private boolean mIsMaskAutoSized;
    
    private View mView;
    private TextView mTitleTextView;
    private TextView mMessageTextView;
    private ImageView mFocusImageView;
    private ImageView mMaskImageView;
    private ImageView mBackgroundImageView;
    
    public SwipeFadeView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public SwipeFadeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);        
        inflater.inflate(R.layout.layout_swipe_fade_view, this, true);
        
        // Define styleables
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeFadeView);
        
        CharSequence titleText = a.getString(R.styleable.SwipeFadeView_titleText);
        int titleTextSize = (int) a.getDimensionPixelOffset(R.styleable.SwipeFadeView_titleTextSize, 0);
        int titleTextColor = (int) a.getColor(R.styleable.SwipeFadeView_titleTextColor, 0);
        
        CharSequence messageText = a.getString(R.styleable.SwipeFadeView_messageText);
        int messageTextSize = (int) a.getDimension(R.styleable.SwipeFadeView_messageTextSize, 0);
        int messageTextColor = (int) a.getColor(R.styleable.SwipeFadeView_messageTextColor, DEFAULT_COLOR);
        
        int focusMaskMargin = (int) a.getDimensionPixelOffset(R.styleable.SwipeFadeView_focusCropMargin, FOCUS_MASK_MARGIN_DEFAULT);
        int focusMaskRadius = (int) a.getDimensionPixelOffset(R.styleable.SwipeFadeView_focusCropRadius, FOCUS_MASK_RADIUS_DEFAULT);
        
        Drawable focusImage = a.getDrawable(R.styleable.SwipeFadeView_focusImage);
        Drawable backgroundImage = a.getDrawable(R.styleable.SwipeFadeView_backgroundImage);
        
        int backgroundBlurRadius = a.getInt(R.styleable.SwipeFadeView_backgroundBlurRadius, BACKGROUND_BLUR_RADIUS_DEFAULT);
        
        // Define view & Prepare settings
        mTitleTextView = (TextView) findViewById(R.id.titleText);
        mMessageTextView = (TextView) findViewById(R.id.messageText);
        mFocusImageView = (ImageView) findViewById(R.id.focusImage);
        mBackgroundImageView = (ImageView) findViewById(R.id.backgroundImage);
        
        mTitleTextView.setText(titleText);
        mMessageTextView.setText(messageText);
        
        mFocusImageView.setImageDrawable(focusImage);
        mBackgroundImageView.setImageBitmap(processBackgroundDrawable(backgroundImage));
        
        if(focusMaskRadius > 0) {
            mIsMaskAutoSized = false;
        } else if(focusMaskMargin > 0) {
            mIsMaskAutoSized = true;
        }
        
    }

    /**
     * Blur the background drawble and return as bitmap.
     * @param backgroundDrawable
     * @return
     */
    private Bitmap processBackgroundDrawable(Drawable backgroundDrawable) {
        Bitmap processedBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
        
        StackBlurManager stackBlurManager = new StackBlurManager(processedBackgroundBitmap);
        stackBlurManager.processRenderScript(mContext, BACKGROUND_BLUR_RADIUS_DEFAULT);
        
        processedBackgroundBitmap = stackBlurManager.returnBlurredImage();
        
        return processedBackgroundBitmap;
    }
    
    private void drawMask(Canvas canvas) {
        canvas.save();
        
        Paint maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        
        Paint imagePaint = new Paint();
        imagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        
        canvas.drawBitmap(getCircleMaskBitmap(),  0,  0, maskPaint);
        canvas.restore();
    }
    
    @SuppressLint("NewApi")
    public Bitmap getCircleMaskBitmap() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        
        float circleRadius = mFocusMaskRadius;
        if(mIsMaskAutoSized) {
            circleRadius =  size.x;//- mFocusCropMargin;
            if(circleRadius < 0) {
                circleRadius *= (-1);
            }
        }
                
        Bitmap circleBitmap = Bitmap.createBitmap(size.x, size.x, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(circleBitmap);
        
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        c.drawCircle(0, 0, circleRadius, paint);
        
        return circleBitmap;
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMask(canvas);
    }
    
    private int convertPxToDp(float px) {
        Resources r = mContext.getResources();
        DisplayMetrics dm = r.getDisplayMetrics();
        
        return (int) (px / (dm.densityDpi / 160f));
    }
    
    public float dpToPx(int dp) {
        Resources r = mContext.getResources();
        DisplayMetrics dm = r.getDisplayMetrics();
        return dp * (dm.densityDpi / 160f);
    }


}
