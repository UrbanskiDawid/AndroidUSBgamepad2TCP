package pl.dawidurbanski.tcpgamepad.VirtualGamePad;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import pl.dawidurbanski.tcpgamepad.R;

/**
 * Created by dawid on 30.01.2016.
 */
public class VirtualGamePadKnobView extends View {

    private Paint
      mBlackPaint = new Paint(),
      mBGPaint = new Paint(),
      mShadowPaing = new Paint(),
      textPaint = new Paint();

    private Rect mRect = new Rect();

    private Knob knob = new Knob();

    private String mxAxisTitle="";
    private String myAxisTitle="";
    Drawable mKnobImage;

    public interface OnEvent { void onMove(float x,float y); }
    public OnEvent onMove = null;

    public VirtualGamePadKnobView(Context context) {
        super(context);
    }

    public VirtualGamePadKnobView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VirtualGamePadKnob,0,0);

        int knobColor=Color.BLACK;
        try {
            mxAxisTitle = ta.getString(R.styleable.VirtualGamePadKnob_xAxisTitle);
            myAxisTitle = ta.getString(R.styleable.VirtualGamePadKnob_yAxisTitle);
            knobColor   = ta.getColor(R.styleable.VirtualGamePadKnob_knobColor,Color.GRAY);
        } finally {
            ta.recycle();
        }
        knob.setColor(knobColor);
    }

    public void disableKnoxXReset()  { knob.resettableX=false; }
    public void disableKnobYReset()  { knob.resettableY=false; }

    public void init() {

        textPaint.setARGB(200, 254, 0, 0);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(24);

        mBlackPaint.setColor(Color.BLACK);
        mShadowPaing.setColor(Color.argb(100,0,0,0));


        int BGcolor = Color.TRANSPARENT;
        Drawable background = getBackground();
        if (background instanceof ColorDrawable)
            BGcolor= ((ColorDrawable) background).getColor();
        mBGPaint.setColor(BGcolor);


        // Specify the path (relative to the 'assets' folder)
        Resources res = getResources();
        mKnobImage = res.getDrawable(android.support.design.R.drawable.abc_btn_radio_to_on_mtrl_000);

        // Set up the user interaction to manually show or hide the system UI.
        setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:

                        knob.set(event.getX(), event.getY(), event.getSize());
                        onNewPosition();
                        break;

                    case MotionEvent.ACTION_UP:
                        knob.reset();
                        onNewPosition();
                        break;
                }
                return true;
            }
        });
    }


    private void onNewPosition()  {
        invalidate();
        if(onMove!=null) {
            onMove.onMove(
              knob.getX(),
              knob.getY());
        }
    }

    public enum HelperLinesStyle {  FALLOW,CENTER,NONE  }

    private class Knob {
        boolean resettableX =true;
        boolean resettableY =true;

        private float xPos=0;//-1.0f - 0.0f
        private float yPos=0;//-1.0f - 0.0f

        float radius=0;//not in uze

        private Paint
                mPaintA = new Paint(),
                mPaintB = new Paint();
        private float sizePx = 25;

        private float deadZoneMin = 0.1f, deadZoneMax = 0.9f;

        private HelperLinesStyle helperLines = HelperLinesStyle.NONE;

        private void setColor(int color){
            mPaintA.setColor(color);
            mPaintA.setStyle(Paint.Style.FILL);

            mPaintB.setColor(Color.argb(100,100,0,0));
            mPaintB.setStyle(Paint.Style.FILL);
        }

        public float getX() { return xPos; }
        public float getY() { return xPos; }

        private float calculatePos(float posPx, int minPx, int maxPx) {

            float x = 2.0f * (-0.5f + (posPx - minPx) / maxPx);

            float abs = Math.abs(x);
            if(abs> deadZoneMax) return deadZoneMax*Math.signum(x);
            if(abs< deadZoneMin) return 0.0f;
            return x;
        }

        public void set(float X,float Y,float size) {
            xPos= calculatePos(X,mRect.left,mRect.right);
            yPos= calculatePos(Y,mRect.top,mRect.bottom);
            radius=size;
        }

        public void reset() {
            if(resettableX) xPos = 0.0f;
            if(resettableY) yPos = 0.0f;
        }

        private float w2=100.0f,h2=100.0f;
        public void resize() {
            w2=mRect.width()/2.0f;
            h2=mRect.height()/2.0f;
        }

        public void draw(Canvas canvas,int width,int height) {

            resize();

            float centerX =w2+xPos*w2;
            float centerY =h2+yPos*h2;

            canvas.drawCircle(centerX,centerY,sizePx,      mPaintB);
            canvas.drawCircle(centerX,centerY,sizePx*0.8f, mPaintA);

            switch(helperLines){
                case CENTER:
                    canvas.drawLine(0, 0, width, height, mBlackPaint);
                    canvas.drawLine(width, 0, 0, height, mBlackPaint);
                break;
                case FALLOW:
                    canvas.drawLine(0, 0,   centerX, centerY, mBlackPaint);
                    canvas.drawLine(width,0, centerX, centerY, mBlackPaint);
                    canvas.drawLine(0, height,   centerX, centerY, mBlackPaint);
                    canvas.drawLine(width,height, centerX, centerY, mBlackPaint);
                break;
                case NONE:
                break;
            }
        }
    }

    public float getXpos() {   return knob.xPos;   }
    public float getYpos() {   return knob.yPos;   }

    @Override
    protected void onDraw(Canvas canvas) {

        getDrawingRect(mRect);

        int h = mRect.height(),
            w = mRect.width();

        float deadZoneProc = 5.0f;

        int dzX = (int)(mRect.width()*deadZoneProc/100.0f),
            dzY = (int)(mRect.width()*deadZoneProc/100.0f);

        canvas.drawRect(0,0,w,h, mShadowPaing);

        //Active part
        canvas.drawRoundRect(new RectF(dzX,dzY,w-dzX,h-dzY), dzX,dzY, mBGPaint);

        //CENTER - deadZone
        canvas.drawRoundRect(new RectF(
                              w/2-dzX,h/2-dzY,
                              w/2+dzX,h/2+dzY), dzX,dzY, mShadowPaing);

        canvas.drawText(myAxisTitle,
                (canvas.getWidth() / 2),
                (int) (dzY*2 - ((textPaint.descent() + textPaint.ascent()) / 2)),
                textPaint);

        canvas.save();
        canvas.rotate(90.0f, 0, 0);
        canvas.drawText(mxAxisTitle,
                (canvas.getHeight() / 2),
                -dzY*2, textPaint);
        canvas.restore();

        //Knob
        knob.draw(canvas,w,h);
    }
}
