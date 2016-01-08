package yhd.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.shapes.Shape;

/**
 * X形状
 * Created by haodongyuan on 2016/1/7.
 */
public class CrossShape extends Shape {
    private float strokeWidth;

    public CrossShape(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float w = getWidth();
        float h = getHeight();

        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);

        // TODO: 重复填充了交点
        canvas.drawLine(0, 0, w, h, paint);
        canvas.drawLine(w, 0, 0, h, paint);
    }
}
