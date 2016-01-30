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

    public void init() {

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

                        knobPos.set(x, y, event.getSize());
                        onNewPosition();
                        break;

                    case MotionEvent.ACTION_UP:
                        knobPos.reset();
                        onNewPosition();
                        break;
                }
                return true;
            }
        });
    }

    private void onNewPosition()  {
        invalidate();
        if(onMove!=null) onMove.onMove(knobPos.xPos,knobPos.yPos);
    }

    private class KnobPosition  {
        boolean mIsInitialized=false;
        boolean resettableX =true;
        boolean resettableY =true;

        float xPos=0;//-1.0f - 0.0f +1.0f
        float yPos=0;//-1.0f - 0.0f +1.0f
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

        public void reset() {
            if(resettableX) xPos = 0.0f;
            if(resettableY) yPos = 0.0f;
        }

        public void draw(Canvas canvas) {
            if(!mIsInitialized) return;

            float w2 = canvas.getWidth()/2;
            float h2 = canvas.getHeight()/2;

            float centerX =w2+xPos*w2;
            float centerY =h2+yPos*h2;

            for(int i=0;i<50;i+=10)
                canvas.drawCircle(
                    centerX,
                    centerY,
                    radius+i,mPaint);
        }
    }

    public float getXpos() {   return knobPos.xPos;   }
    public float getYpos() {   return knobPos.yPos;   }

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
