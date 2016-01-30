package pl.dawidurbanski.tcpgamepad.VirtualGamePad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dawid on 30.01.2016.
 */
public class VirtualGamePadKnobView extends View {

    private Paint mPaint = new Paint();

    public String mName="";

    public float
        xPos = 0.0f,
        yPos = 0.0f;

    private Rect mRect = new Rect();

    private KnobPosition knobPos = new KnobPosition();

    public interface OnEvent { void onMove(float x,float y); }
    public OnEvent onMove = null;

    public VirtualGamePadKnobView(Context context) {
        super(context);
        mPaint.setColor(Color.BLACK);
    }

    public void disableKnoxXReset()  { knobPos.resettableX=false; }
    public void disableKnobYReset()  { knobPos.resettableY=false; }

    public void init(String name) {
        mName=name;

        // Set up the user interaction to manually show or hide the system UI.
        setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getDrawingRect(mRect);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:

                        float x = 2.0f * (-0.5f + (event.getX() - mRect.left) / mRect.right);
                        float y = 2.0f * (-0.5f + (event.getY() + mRect.top) / mRect.bottom);

                        knobPos.set(event.getX(), event.getY(), event.getSize());
                        onAnalogMove(x, y);
                        invalidate();
                        break;

                    case MotionEvent.ACTION_UP:
                        knobPos.reset(mRect.centerX(), mRect.centerY());
                        onAnalogMove(0.0f, 0.0f);
                        invalidate();
                        break;
                }
                return true;
            }
        });
    }

    private class KnobPosition  {
        boolean mIsInitialized=false;
        boolean resettableX =true;
        boolean resettableY =true;

        float xPos=0;
        float yPos=0;
        float radius=0;

        public void KnobPosition(boolean x,boolean y) {
            resettableX=x;
            resettableX=y;
        }

        public void set(float x,float y,float size) {
            mIsInitialized =true;
            xPos=x;
            yPos=y;
            radius=size;
        }

        public void reset(float x,float y) {
            if(resettableX) xPos = x;
            if(resettableY) yPos = y;
        }

        public void draw(Canvas canvas){
            if(!mIsInitialized) return;
            for(int i=0;i<50;i+=10)
            canvas.drawCircle(xPos,yPos,radius+i,mPaint);
        }
    }

    private void onAnalogMove(float x,float y) {

        xPos = x;
        yPos = y;

        if(onMove!=null) onMove.onMove(xPos,yPos);
    }

    public VirtualGamePadKnobView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int w = canvas.getWidth();
        int h = canvas.getHeight();

        //X
        canvas.drawLine(0, 0, w, h, mPaint);
        canvas.drawLine(w, 0, 0, h, mPaint);

        //Line from center
        knobPos.draw(canvas);
    }
}
