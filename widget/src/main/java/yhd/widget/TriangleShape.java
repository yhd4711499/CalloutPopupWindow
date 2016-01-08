package yhd.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.shapes.Shape;

/**
 * 三角形
 * Created by haodongyuan on 2016/1/7.
 */
public class TriangleShape extends Shape {
    private Direction direction;
    Path path = new Path();
    public TriangleShape(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float w = getWidth();
        float h = getHeight();

        getEquilateralTriangle(w, h, direction);

        canvas.drawPath(path, paint);
    }

    private void getEquilateralTriangle(float width, float height, Direction direction) {
        PointF p1 = null, p2 = null, p3 = null;

        if (direction == Direction.NORTH) {
            p1 = new PointF(0, height);
            p2 = new PointF(width, height);
            p3 = new PointF((width / 2), 0);
        } else if (direction == Direction.SOUTH) {
            p1 = new PointF(0, 0);
            p2 = new PointF(width, 0);
            p3 = new PointF((width / 2), height);
        } else if (direction == Direction.EAST) {
            p1 = new PointF(0, 0);
            p2 = new PointF(0, height);
            p3 = new PointF(width, (height / 2));
        } else if (direction == Direction.WEST) {
            p1 = new PointF(width, 0);
            p2 = new PointF(0, (height / 2));
            p3 = new PointF(width, height);
        }

        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
    }

    public enum Direction {
        NORTH, SOUTH, EAST, WEST;
    }
}
