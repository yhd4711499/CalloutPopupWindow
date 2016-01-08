package yhd.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * 用来显示这种浮层：<p>
 * <img src="http://tapd.oa.com/tfl/captures/2015-11/tapd_10028191_base64_1447640081_28.png"/>
 * <p>可以使用{@link CalloutPopupWindow#builder(Context)}来创建图中的简单文字浮层
 * <p>见：https://github.com/pinguo-marui/PointerPopupWindow
 */
public class CalloutPopupWindow extends PopupWindow implements View.OnClickListener {
    private static final java.lang.String TAG = "CalloutPopupWindow";

    private final static int MAX_WIDTH_DP = 250;
    private final static int MAX_HEIGHT_DP = 120;
    private static final int MSG_DISMISS = 1;

    private LinearLayout mContainer;
    private ImageView mAnchorImage;
    private FrameLayout mContent;

    private int mMarginScreen;
    private AlignMode mAlignMode = AlignMode.DEFAULT;
    private boolean dismissWhenTouchOutside = true;
    /**
     * 多少秒之后消失
     */
    private int lifeTime = 0;

    private Handler handler = new InnerHandler();

    private Position position;
    private int upPointerRes;
    private int leftPointerRes;
    private Drawable upPointerDrawable;
    private Drawable leftPointerDrawable;

    /**
     * 创建简单文字浮层
     * @param context activity
     * @return {@link Builder}
     */
    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public CalloutPopupWindow(Context context, int width, Position position) {
        this(context, width, ViewGroup.LayoutParams.WRAP_CONTENT, position);
    }

    public CalloutPopupWindow(Context context, int width, int height, Position position) {
        super(width, height);
        if(width < 0) {
            throw new RuntimeException("You must specify the window width explicitly(do not use WRAP_CONTENT or MATCH_PARENT)!!!");
        }
        this.position = position;
        mContainer = new LinearLayout(context);
        mContainer.setPadding(0, 0, 0, 0);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        mAnchorImage = new ImageView(context);
        mContent = new FrameLayout(context);
        setBackgroundDrawable(new ColorDrawable());
        setOutsideTouchable(true);
        setFocusable(false);
    }

    public AlignMode getAlignMode() {
        return mAlignMode;
    }

    public void setAlignMode(AlignMode mAlignMode) {
        this.mAlignMode = mAlignMode;
    }

    public int getMarginScreen() {
        return mMarginScreen;
    }

    public void setMarginScreen(int marginScreen) {
        this.mMarginScreen = marginScreen;
    }

    public void setPointerImageRes(int upRes, int leftRes) {
        upPointerRes = upRes;
        leftPointerRes = leftRes;
    }

    public void setPointerImageDrawable(Drawable up, Drawable left) {
        upPointerDrawable = up;
        leftPointerDrawable = left;
    }

    /**
     * 调用之前请调用{@link #setPointerImageRes}设置好图像
     */
    @Override
    public void setContentView(View contentView) {
        if (contentView != null) {
            mContainer.removeAllViews();
            switch (position) {
                case ABOVE:
                    mContainer.addView(mContent, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mContainer.addView(mAnchorImage, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (upPointerDrawable != null) {
                        mAnchorImage.setImageDrawable(upPointerDrawable);
                    } else {
                        mAnchorImage.setImageResource(upPointerRes);
                    }
                    flipImageView(mAnchorImage);
                    break;
                case BELOW:
                    mContainer.addView(mAnchorImage, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mContainer.addView(mContent, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (upPointerDrawable != null) {
                        mAnchorImage.setImageDrawable(upPointerDrawable);
                    } else {
                        mAnchorImage.setImageResource(upPointerRes);
                    }
                    break;
                case LEFT:
                    mContainer.setOrientation(LinearLayout.HORIZONTAL);
                    mContainer.addView(mContent, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mContainer.addView(mAnchorImage, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (leftPointerDrawable != null) {
                        mAnchorImage.setImageDrawable(leftPointerDrawable);
                    } else {
                        mAnchorImage.setImageResource(leftPointerRes);
                    }
                    flipImageView(mAnchorImage);
                    break;
                case RIGHT:
                    mContainer.setOrientation(LinearLayout.HORIZONTAL);
                    mContainer.addView(mAnchorImage, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    mContainer.addView(mContent, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (leftPointerDrawable != null) {
                        mAnchorImage.setImageDrawable(leftPointerDrawable);
                    } else {
                        mAnchorImage.setImageResource(leftPointerRes);
                    }
                    break;
            }

            mContent.addView(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            super.setContentView(mContainer);
        }
    }

    /**
     * 转180度
     */
    private void flipImageView(ImageView imageView) {
        Rect originalBounds = new Rect(0, 0, imageView.getDrawable().getIntrinsicWidth(), imageView.getDrawable().getIntrinsicHeight());
        Matrix matrix = new Matrix();
        imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
        matrix.postRotate(180, originalBounds.width() /2, originalBounds.height() /2);
        imageView.setImageMatrix(matrix);
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        //deprecation
        mContent.setBackgroundDrawable(background);
        super.setBackgroundDrawable(new ColorDrawable());
    }

    @Override
    public void dismiss() {
        handler.removeMessages(MSG_DISMISS);
        super.dismiss();
    }

    /**
     * @param anchor 显示在哪个元素下面？
     */
    public void showAsPointer(View anchor) {
        showAsPointer(anchor, 0, 0);
    }

    /**
     * @param anchor anchor 显示在哪个元素下面？
     * @param yoff 浮层y偏移像素
     */
    public void showAsPointer(View anchor, int yoff) {
        showAsPointer(anchor, 0, yoff);
    }

    /**
     * @param anchor anchor 显示在哪个元素下面？
     * @param xoff 浮层x偏移像素
     * @param yoff 浮层y偏移像素
     */
    public void showAsPointer(final View anchor, final int xoff, final int yoff) {
        // 保证anchor已经measured
        if (anchor.getWidth() == 0 && anchor.getVisibility() == View.VISIBLE) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showInternal(anchor, xoff, yoff);
                }
            });
        } else {
            showInternal(anchor, xoff, yoff);
        }
    }

    private void showInternal(View anchor, int xoff, int yoff) {
        try {
            int maxWidth = DpPxUtil.dip2px(anchor.getContext(), MAX_WIDTH_DP);
            int maxHeight = DpPxUtil.dip2px(anchor.getContext(), MAX_HEIGHT_DP);
            mContent.measure(maxWidth, maxHeight);

            // get location and size
            final Rect displayFrame = new Rect();
            anchor.getWindowVisibleDisplayFrame(displayFrame);
            final int displayFrameWidth = displayFrame.right - displayFrame.left;
            final int displayFrameHeight = displayFrame.bottom - displayFrame.top;
            int[] loc = new int[2];
            anchor.getLocationInWindow(loc);//get anchor location

            if (position.horizontal) {
                int anchorImageWidth = mAnchorImage.getDrawable().getIntrinsicWidth();

                // enlarge self
                setWidth(getWidth() + anchorImageWidth);

                if (position == Position.LEFT) {
                    xoff = -1 * (mContent.getMeasuredWidth() + anchorImageWidth);
                } else if (position == Position.RIGHT) {
                    xoff = (anchor.getWidth() + anchorImageWidth);
                }
                if (mAlignMode == AlignMode.AUTO_OFFSET) {
                    float offCenterRate = (displayFrame.centerY() - loc[1]) / (float) displayFrameHeight;
                    yoff = -1 * (int) ((anchor.getHeight() + mContent.getMeasuredHeight()) / 2 - offCenterRate * mContent.getMeasuredWidth() / 2);
                } else if (mAlignMode == AlignMode.AUTO_OFFSET.CENTER_FIX) {
                    yoff = -1 * (anchor.getHeight() + mContent.getMeasuredHeight()) / 2;
                }
            } else {
                int anchorImageHeight = mAnchorImage.getDrawable().getIntrinsicHeight();
                if (position == Position.ABOVE) {
                    yoff = -1 * (mContent.getMeasuredHeight() + anchor.getHeight() + anchorImageHeight);
                }
                if (mAlignMode == AlignMode.AUTO_OFFSET) {
                    float offCenterRate = (displayFrame.centerX() - loc[0]) / (float) displayFrameWidth;
                    xoff = (int) ((anchor.getWidth() - getWidth()) / 2 + offCenterRate * getWidth() / 2);
                } else if (mAlignMode == AlignMode.AUTO_OFFSET.CENTER_FIX) {
                    xoff = (anchor.getWidth() - getWidth()) / 2;
                }
            }

            int left = loc[0] + xoff;
            int right = left + getWidth();
            int top = loc[1] + yoff;
            int bottom = top + mContent.getMeasuredHeight();

            int yAnchorOff = 0;

            if (position.horizontal) {
                // reset y offset to display the window fully in the screen
                if (bottom > displayFrameHeight - mMarginScreen) {
                    yAnchorOff = bottom - (displayFrameHeight - mMarginScreen);
                }
                if (top < displayFrame.top + mMarginScreen) {
                    yAnchorOff = displayFrame.top + mMarginScreen - top;
                }
            } else {
                // reset x offset to display the window fully in the screen
                if (right > displayFrameWidth - mMarginScreen) {
                    xoff = (displayFrameWidth - mMarginScreen - getWidth()) - loc[0];
                }
                if (left < displayFrame.left + mMarginScreen) {
                    xoff = displayFrame.left + mMarginScreen - loc[0];
                }
            }
            computePointerLocation(anchor, xoff, yAnchorOff);

            super.showAsDropDown(anchor, xoff, yoff + yAnchorOff);

            if (lifeTime > 0) {
                handler.sendMessageDelayed(handler.obtainMessage(MSG_DISMISS, new WeakReference<CalloutPopupWindow>(this)), lifeTime * 1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "[showInternal] failed to show window", e);
        }
    }

    private void computePointerLocation(View anchor, int xoff, int yoff) {
        Drawable drawable = mAnchorImage.getDrawable();
        if (position.horizontal) {
            int ph = mContent.getMeasuredHeight();
            int dh = drawable.getIntrinsicHeight();
            mAnchorImage.setPadding(0, (ph - dh) / 2 - yoff, 0, 0);
        } else {
            int aw = anchor.getWidth();
            int dw = drawable.getIntrinsicWidth();
            mAnchorImage.setPadding((aw - dw) / 2 - xoff, 0, 0, 0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_callout_close) {
            this.dismiss();
        }
    }

    public enum Position {
        ABOVE(false),
        BELOW(false),
        LEFT(true),
        RIGHT(true);

        private boolean horizontal;

        Position(boolean horizontal) {
            this.horizontal = horizontal;
        }
    }


    public enum AlignMode {
        /**
         * default align mode,align the left|bottom of the anchor view
         */
        DEFAULT,
        /**PopupWindowMain
         * align center of the anchor view
         */
        CENTER_FIX,
        /**
         * according to the location of the anchor view in the display window,
         * auto offset the popup window to display center.
         */
        AUTO_OFFSET
    }

    /**
     * 使用这个来包装一下{@link Builder}，可以方便地创建自定义颜色，圆角的样式
     */
    public static class DrawableBuilder {
        private Builder windowBuilder;
        private int radius;
        private int color;
        private int pointerWidth = 12;
        private int pointerHeight = 8;
        private float closeButtonStrokeWidth = 1;
        private int closeButtonColor = Color.parseColor("#AAffffff");
        private int closeButtonSize = 8;

        public DrawableBuilder(Builder windowBuilder) {
            this.windowBuilder = windowBuilder;
        }

        public Builder build() {
            Drawable bg = createRoundedRectDrawable(dp2px(radius), color);
            windowBuilder.setBackgroundDrawable(bg);

            Drawable upPointer = createTriangleDrawable(dp2px(pointerWidth), dp2px(pointerHeight), Position.ABOVE, color);
            Drawable leftPointer = createTriangleDrawable(dp2px(pointerHeight), dp2px(pointerWidth), Position.LEFT, color);

            windowBuilder.setLeftPointerDrawable(leftPointer);
            windowBuilder.setUpPointerDrawable(upPointer);

            int closeButtonSizePx = dp2px(closeButtonSize);
            Drawable close = createCloseButtonDrawable(closeButtonSizePx, closeButtonSizePx, closeButtonColor, dp2px(closeButtonStrokeWidth));

            windowBuilder.setCloseButtonDrawable(close);

            return windowBuilder;
        }

        private int dp2px(float dp) {
            return DpPxUtil.dip2px(windowBuilder.context, dp);
        }

        /**
         * @param width 关闭按钮的粗细，dp
         */
        public DrawableBuilder setCloseButtonStrokeWidth (float width){
            closeButtonStrokeWidth = width;
            return this;
        }

        /**
         * @param color 关闭按钮的颜色
         */
        public DrawableBuilder setCloseButtonColor (int color) {
            closeButtonColor = color;
            return this;
        }

        /**
         * @param radius 背景圆角大小，dp
         */
        public DrawableBuilder setBackgroundRadius (int radius) {
            this.radius = radius;
            return this;
        }

        /**
         * @param color 背景颜色
         */
        public DrawableBuilder setBackgroundColor (int color) {
            this.color = color;
            return this;
        }

        /**
         * 指针大小，dp
         */
        public DrawableBuilder setPointerSize(int pointerWidth, int pointerHeight) {
            this.pointerWidth = pointerWidth;
            this.pointerHeight = pointerHeight;
            return this;
        }

        private static Drawable createRoundedRectDrawable (float r, int color) {
            RoundRectShape rect = new RoundRectShape(new float[]{r, r, r, r, r, r, r, r}, null, null);
            ShapeDrawable drawable = new ShapeDrawable(rect);
            drawable.getPaint().setColor(color);
            return drawable;
        }

        private static Drawable createTriangleDrawable (int width, int height, Position position, int color) {
            TriangleShape triangleShape = new TriangleShape(getDirection(position));
            triangleShape.resize(width, height);
            ShapeDrawable drawable = new ShapeDrawable(triangleShape);
            drawable.setIntrinsicWidth(width);
            drawable.setIntrinsicHeight(height);
            drawable.getPaint().setColor(color);
            return drawable;
        }

        private static Drawable createCloseButtonDrawable (int width, int height, int color, float strokeWidth) {
            CrossShape shape = new CrossShape(strokeWidth);
            shape.resize(width, height);
            ShapeDrawable drawable = new ShapeDrawable(shape);
            drawable.setIntrinsicWidth(width);
            drawable.setIntrinsicHeight(height);
            drawable.getPaint().setColor(color);
            return drawable;
        }

        private static TriangleShape.Direction getDirection(Position position) {
            switch (position) {
                case ABOVE:
                    return TriangleShape.Direction.NORTH;
                case BELOW:
                    return TriangleShape.Direction.SOUTH;
                case LEFT:
                    return TriangleShape.Direction.WEST;
                case RIGHT:
                    return TriangleShape.Direction.EAST;
            }
            return null;
        }
    }

    /**
     * 简单的文字浮层Builder，显示文字和关闭按钮<p>
     * 一般只要设置{@link #setText(String)}即可
     */
    public static class Builder {
        private Context context;
        private String text;
        private int textColor = Color.WHITE;
        private int upPointerResource = android.R.color.transparent;
        private int leftPointerResource = android.R.color.transparent;
        private boolean autoDismiss = true;
        private boolean showCloseButton = true;
        private int lifeTime = 5;
        private Position position = Position.BELOW;
        private Drawable backgroundDrawable;
        private Drawable upPointerDrawable;
        private Drawable leftPointerDrawable;
        private Drawable closeButtonDrawable;

        private Builder(Context context) {
            this.context = context;
        }

        /**
         * @param text 文字
         */
        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        /**
         * @param color 文字颜色，默认白色
         */
        public Builder setTextColor(int color) {
            this.textColor = color;
            return this;
        }

        /**
         * @param pointerResource 指针样式
         */
        public Builder setUpPointerResource(int pointerResource) {
            this.upPointerResource = pointerResource;
            this.upPointerDrawable = null;
            return this;
        }

        /**
         * @param pointerResource 指针样式
         */
        public Builder setLeftPointerResource(int pointerResource) {
            this.leftPointerResource = pointerResource;
            this.leftPointerDrawable = null;
            return this;
        }

        /**
         * @param upPointerDrawable 指针样式
         */
        public Builder setUpPointerDrawable(Drawable upPointerDrawable) {
            this.upPointerDrawable = upPointerDrawable;
            return this;
        }

        /**
         * @param leftPointerDrawable 指针样式
         */
        public Builder setLeftPointerDrawable(Drawable leftPointerDrawable) {
            this.leftPointerDrawable = leftPointerDrawable;
            return this;
        }

        /**
         * @param autoDismiss 点击外部是否关闭此浮层
         */
        public Builder setAutoDismiss(boolean autoDismiss) {
            this.autoDismiss = autoDismiss;
            return this;
        }

        /**
         * @param showCloseButton 是否显示关闭按钮，默认true
         */
        public Builder setShowCloseButton(boolean showCloseButton) {
            this.showCloseButton = showCloseButton;
            return this;
        }

        /**
         * @param seconds 定时关闭，秒，默认5
         */
        public Builder setLifetime(int seconds) {
            this.lifeTime = seconds;
            return this;
        }

        /**
         * @param position 相对位置，如果是上下，可以使用{@link #setUpPointerResource(int)}设置指针样式；如果是左右，可以使用{@link #setLeftPointerResource(int)}设置
         */
        public Builder setPosition(Position position) {
            this.position = position;
            return this;
        }

        /**
         * @param drawable 背景图
         */
        public Builder setBackgroundDrawable(Drawable drawable) {
            this.backgroundDrawable = drawable;
            return this;
        }

        public Builder setCloseButtonDrawable(Drawable drawable) {
            this.closeButtonDrawable = drawable;
            return this;
        }

        /**
         * @return 失败的话会返回null!
         */
        public CalloutPopupWindow build() {
            try {
                int maxWidth = DpPxUtil.dip2px(context, MAX_WIDTH_DP);
                int maxHeight = DpPxUtil.dip2px(context, MAX_HEIGHT_DP);
                View view = LayoutInflater.from(context).inflate(R.layout.callout_common, null, false);
                if (backgroundDrawable != null) {
                    int paddingLeft = view.getPaddingLeft();
                    int paddingTop = view.getPaddingTop();
                    int paddingRight = view.getPaddingRight();
                    int paddingBottom = view.getPaddingBottom();
                    view.setBackgroundDrawable(backgroundDrawable);
                    // 重新设置background后 padding会丢失，这里要重新恢复
                    view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                }
                TextView textView = (TextView) view.findViewById(R.id.text_callout_content);
                textView.setMaxWidth(maxWidth);
                textView.setMaxHeight(maxHeight);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setText(text);
                textView.setTextColor(textColor);
                view.measure(maxWidth, maxHeight);

                CalloutPopupWindow pop = new CalloutPopupWindow(context, view.getMeasuredWidth(), position);

                ImageButton button = (ImageButton) view.findViewById(R.id.button_callout_close);
                if (button != null) {
                    if (showCloseButton) {
                        button.setVisibility(View.VISIBLE);
                        if (closeButtonDrawable != null) {
                            button.setImageDrawable(closeButtonDrawable);
                        }
                    } else {
                        button.setVisibility(View.GONE);
                    }
                    button.setOnClickListener(pop);
                }

                pop.setPointerImageRes(upPointerResource, leftPointerResource);
                pop.setPointerImageDrawable(upPointerDrawable, leftPointerDrawable);
                pop.setContentView(view);
                pop.setAlignMode(AlignMode.AUTO_OFFSET);
                pop.dismissWhenTouchOutside = autoDismiss;
                if (!autoDismiss) {
                    pop.setOutsideTouchable(false);
                }
                // 离屏幕边缘6dp
                pop.setMarginScreen(DpPxUtil.dip2px(context, 6));

                pop.lifeTime = lifeTime;
                return pop;
            } catch (Throwable e) {
                Log.e(TAG, "[build] failed to build window", e);
            }
            return null;
        }

        public CalloutPopupWindow buildByView(View view) {
            try {
                CalloutPopupWindow pop = new CalloutPopupWindow(context,
                        view.getMeasuredWidth(),
                        position);

                pop.setPointerImageRes(upPointerResource, leftPointerResource);
                pop.setContentView(view);
                pop.setAlignMode(AlignMode.AUTO_OFFSET);
                pop.dismissWhenTouchOutside = autoDismiss;
                pop.lifeTime = lifeTime;
                return pop;
            } catch (Throwable e) {
                Log.e(TAG, "[build] failed to build window", e);
            }
            return null;
        }
    }

    public void showAtLocationAndHidePointer(View parent, int gravity, int x, int y) {
        Drawable drawable = mAnchorImage.getDrawable();
        measureView(mContent);
        if (position.horizontal) {
            int aw = mContent.getMeasuredWidth();
            int dw = drawable.getIntrinsicWidth();
            showAtLocation(parent, gravity, x + dw, y);
        } else {
            int ph = mContent.getMeasuredHeight();
            int dh = drawable.getIntrinsicHeight();
            showAtLocation(parent, gravity, x, y - ph - dh);
        }
        mAnchorImage.setVisibility(View.GONE);
    }

    public void updatePosition(int x, int y) {
        Drawable drawable = mAnchorImage.getDrawable();
        if (position.horizontal) {
            int aw = mContent.getMeasuredWidth();
            int dw = drawable.getIntrinsicWidth();
            update(x + dw, y, -1, -1);
        } else {
            int ph = mContent.getMeasuredHeight();
            int dh = drawable.getIntrinsicHeight();
            update(x, y - ph - dh, -1, -1);
        }
    }

    private static void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int lpWidth = p.width;
        int childWidthSpec;
        if (lpWidth > 0) {
            childWidthSpec = View.MeasureSpec.makeMeasureSpec(lpWidth,
                    View.MeasureSpec.EXACTLY);
        } else {
            childWidthSpec = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
        }

        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight,
                    View.MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
        }

        child.measure(childWidthSpec, childHeightSpec);
    }

    private static class InnerHandler extends Handler {
        public InnerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISMISS:
                    if (msg.obj != null) {
                        WeakReference reference = (WeakReference) msg.obj;
                        Object o = reference.get();
                        if (o != null && o instanceof CalloutPopupWindow) {
                            ((CalloutPopupWindow) o).dismiss();
                        }
                    }
                    break;
                default:
                    break;

            }
        }
    }
}