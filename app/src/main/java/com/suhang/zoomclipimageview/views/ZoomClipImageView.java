package com.suhang.zoomclipimageview.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by sh on 2015/12/9.
 */
public class ZoomClipImageView extends ImageView implements View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener {
	private float initScale = 1.0f;
	private final float MAX_SCALE = 4.0f;
	private float width;
	private float height;
	private ScaleGestureDetector scaleDetector;
	private GestureDetector detector;
	private Matrix scaleMatrix = new Matrix();
	private Drawable d;
	private float dw;
	private float dh;
	private Context context;
	private boolean isInvoke = false;
	private float[] matrixValues = new float[9];
	public static final float MID_SCALE = 2.0f;
	private boolean isDoubleTab = false;
	private boolean isAnimationOver = true;
	//初始截图框大小
	private int size;

	private float left;
	private float right;
	private float top;
	private float bottom;
	private float clipW;
	private float clipH;
	private Paint paint;
	private float borderW;
	private float ballSize;
	private RectF rectFone;
	private RectF rectFTwo;
	private RectF rectFThree;
	private RectF rectFFour;


	public ZoomClipImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setScaleType(ScaleType.MATRIX);
		paint = new Paint();
		paint.setAntiAlias(true);
		borderW = dip2px(context, 1);
		ballSize = dip2px(context, 30);
		size = dip2px(context, 100);
		setBackgroundColor(0xff000000);
		scaleDetector = new ScaleGestureDetector(context, this);
		detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {//检点的双击屏幕的事件处理
				float scale = getCurrentScale();
				if (isAnimationOver) {//双击屏幕进行放大图片动画
					if (scale >= initScale && scale < MID_SCALE) {
						if (!isDoubleTab) {
							doubleTabAnimation(scale, MID_SCALE);
							isDoubleTab = true;
							return true;
						}
					}

					if (scale >= MID_SCALE && scale <= MAX_SCALE) {
						if (!isDoubleTab) {
							doubleTabAnimation(scale, MAX_SCALE);
							isDoubleTab = true;
							return true;
						}
					}
					if (isDoubleTab) {
						doubleTabAnimation(scale, initScale);
						isDoubleTab = false;
						return true;
					}
				}
				return true;
			}

		});
		rectFone = new RectF();
		rectFTwo = new RectF();
		rectFThree = new RectF();
		rectFFour = new RectF();
		this.setOnTouchListener(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {//在测量控件大小后初始化截图矩形的上下左右位置和区域宽高
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		left = getWidth() / 2 - size;
		top = getHeight() / 2 - size;
		right = getWidth() / 2 + size;
		bottom = getHeight() / 2 + size;
		clipW = right - left;
		clipH = bottom - top;
	}

	/**
	 * 设置截图框大小
	 *
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//先画出矩形之外的透明的黑色阴影
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(0xaa000000);
		canvas.drawRect(0, 0, left, getHeight(), paint);
		canvas.drawRect(right, 0, getWidth(), getHeight(), paint);
		canvas.drawRect(left, 0, right, top, paint);
		canvas.drawRect(left, bottom, right, getHeight(), paint);
		// 再画出空心的矩形部分
		paint.setColor(0xffffffff);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(borderW);
		canvas.drawRect(left, top, right, bottom, paint);
		//最后画出可控制矩形大小的四个按钮
		paint.setColor(0xff6393e7);
		paint.setStyle(Paint.Style.FILL);
		rectFone.left = left - ballSize / 2 - borderW;
		rectFone.top = top - ballSize / 2 - borderW;
		rectFone.right = left + ballSize / 2 - borderW;
		rectFone.bottom = top + ballSize / 2 - borderW;
		rectFTwo.left = right - ballSize / 2 + borderW;
		rectFTwo.top = top - ballSize / 2 - borderW;
		rectFTwo.right = right + ballSize / 2 - borderW + borderW;
		rectFTwo.bottom = top + ballSize / 2 - borderW;
		rectFThree.left = left - ballSize / 2 - borderW;
		rectFThree.top = bottom - ballSize / 2 + borderW;
		rectFThree.right = left + ballSize / 2 - borderW;
		rectFThree.bottom = bottom + ballSize / 2 + borderW;
		rectFFour.left = right - ballSize / 2 + borderW;
		rectFFour.top = bottom - ballSize / 2 + borderW;
		rectFFour.right = right + ballSize / 2 + borderW;
		rectFFour.bottom = bottom + ballSize / 2 + borderW;
		//canvas.drawArc(rectFone, 0, -270, true, paint);
		//canvas.drawArc(rectFTwo, -180, 270, true, paint);
		//canvas.drawArc(rectFThree, 0, 270, true, paint);
		//canvas.drawArc(rectFFour, -90, 270, true, paint);

	}

	private int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private void doubleTabAnimation(final float currentScale, final float endScale) {//双击放大图片的动画
		isAnimationOver = false;
		final ValueAnimator va = ValueAnimator.ofFloat(currentScale, endScale);
		va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			float lastValue = currentScale;//记录上一次的值

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (float) animation.getAnimatedValue();
				float bili = value / lastValue;//求出每次的变化比例
				scaleMatrix.postScale(bili, bili, width / 2, height / 2);
				setImageMatrix(scaleMatrix);
				lastValue = value;
			}
		});
		va.setDuration(1000).start();
		va.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				isAnimationOver = true;
			}
		});
	}

	public Bitmap getClipBitmap() {//对外提供的得到截图位图的方法
		Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		draw(c);//将当前控件所显示的画面作为内容填充到位图中
		return Bitmap.createBitmap(b, (int) (left + borderW), (int) (top + borderW), (int) (right - left - 2 * borderW), (int) (bottom - top - 2 * borderW));
	}

	public ZoomClipImageView(Context context) {
		super(context);
	}

	@Override
	public void onGlobalLayout() {//当全局布局显示在控件中时调用此方法,当视图树发生变化时也会调用此方法,用于初始化,能得到整个控件的大小
		if (!isInvoke) {//该方法可能会被调用多次,防止多次调用
			width = getWidth();
			height = getHeight();
			d = getDrawable();
			if (d == null) {
				return;
			}
			dw = d.getIntrinsicWidth();
			dh = d.getIntrinsicHeight();
			//当加载的图片大于屏幕宽或者高时,分别得到缩放比例
			if (dw > width) {
				initScale = width / dw;
			}
			if (dh > height) {
				initScale = height / dh;
			}

			if (dw > width && dh > height) {
				initScale = Math.min(width / dw, height / dh);
			}
			if (dw < width && dh < height) {
				initScale = 1.0f;
			}
			scaleMatrix.reset();//不重置矩阵值不正确
			scaleMatrix.postTranslate(width / 2 - dw / 2, height / 2 - dh / 2);//按照比例将图片缩放到屏幕大小,并显示在屏幕中间
			scaleMatrix.postScale(initScale, initScale, width / 2, height / 2);
			setImageMatrix(scaleMatrix);
			isInvoke = true;
		}
	}


	private float x = 0, y = 0;

	private boolean isTouchClip;
	private int touchChange = -1;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		scaleDetector.onTouchEvent(event);//分配手势缩放事件
		RectF rf = getRect();//得到存有图片上下左右位置的矩阵
		if (detector.onTouchEvent(event)) {//分配双击屏幕事件,如果是双击屏幕,就不用处理其他事件,直接返回
			return true;
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN://一个手指按下后,其他手指再按下,只会响应一次DOWN事件
				if (rf.width() > width || rf.height() > height) {  //当放在viewpager中,触摸移动会被拦截,用此判断阻止拦截
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				x = event.getX();
				y = event.getY();

//				if (x > left && x < right && y > top && y < bottom) {//是否触摸到矩形区域的判断
//					isTouchClip = true;
//				}

				if ((x >= left - ballSize / 2 && x <= left + ballSize / 2 && y >= top - ballSize / 2 && y <= top + ballSize / 2)) {//四个控制截图矩形大小的按钮的触摸判断
					touchChange = 1;
				}

				if ((x >= right - ballSize / 2 && x <= right + ballSize / 2 && y >= top - ballSize / 2 && y <= top + ballSize / 2)) {
					touchChange = 2;
				}

				if ((x >= left - ballSize / 2 && x <= left + ballSize / 2 && y >= bottom - ballSize / 2 && y <= bottom + ballSize / 2)) {
					touchChange = 3;
				}

				if ((x >= right - ballSize / 2 && x <= right + ballSize / 2 && y >= bottom - ballSize / 2 && y <= bottom + ballSize / 2)) {
					touchChange = 4;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				float x1 = event.getX();
				float y1 = event.getY();
				if (isTouchClip) {//如果按在矩形框上,则可以移动矩形框
					left = x1 - clipW / 2;
					right = left + clipW;
					top = y1 - clipH / 2;
					bottom = top + clipH;
					if (left <= 0) {
						x1 = clipW / 2;
					}
					if (right >= getWidth()) {
						x1 = getWidth() - clipW / 2;
					}

					if (top <= 0) {
						y1 = clipH / 2;
					}
					if (bottom >= getHeight()) {
						y1 = getHeight() - clipH / 2;
					}
					left = x1 - clipW / 2;
					right = x1 + clipW / 2;
					top = y1 - clipH / 2;
					bottom = y1 + clipH / 2;
					invalidate();
					return true;
				}
				if (rf.width() > width || rf.height() > height) {//防止和ViewPager的移动逻辑冲突
					getParent().requestDisallowInterceptTouchEvent(true);//请求父控件不拦截触摸事件
				}
				float dx = x1 - x;
				float dy = y1 - y;
				if ((rf.left == 0 && dx > 0) || (rf.right == width && dx < 0)) {
					getParent().requestDisallowInterceptTouchEvent(false);
				}
//				if (rf.width() < width) {
//					dx = 0;
//				}
//				if (rf.height() < height) {
//					dy = 0;
//				}
				scaleMatrix.postTranslate(dx, dy);
				//controllImage();
				setImageMatrix(scaleMatrix);
				x = x1;
				y = y1;
				break;
			case MotionEvent.ACTION_UP:
				isTouchClip = false;
				touchChange = -1;
				break;
			default:
				break;
		}
		return true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			getViewTreeObserver().removeOnGlobalLayoutListener(this);//此方法无法兼容低版本
		}
	}

	@Override
	protected void onAttachedToWindow() {//添加全局监听事件
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);//坚挺窗口中布局的改变
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {//根据缩放手势来缩放图片
		if (d == null) {
			return true;
		}
		float scale = getCurrentScale();
		float factor = detector.getScaleFactor();
		if ((scale < MAX_SCALE && factor > 1.0f) || (scale >= initScale && factor < 1.0f)) {
			if (scale * factor > MAX_SCALE) {
				factor = MAX_SCALE / scale;
			}
			if (scale * factor < initScale) {
				factor = initScale / scale;
			}

			//controllImage();
			scaleMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
			setImageMatrix(scaleMatrix);
		}
		return true;
	}

	private void controllImage() {//控制图片的缩放和移动不会使屏幕出现白边
		float deltaX = 0;
		float deltaY = 0;
		RectF rf = getRect();
		if (rf.width() >= width) {
			if (rf.left >= 0) {
				deltaX = -rf.left;
			}

			if (rf.right <= width) {
				deltaX = width - rf.right;
			}
		}

		if (rf.height() >= height) {
			if (rf.top >= 0) {
				deltaY = -rf.top;
			}
			if (rf.bottom <= height) {
				deltaY = height - rf.bottom;
			}
		}
		if (rf.width() <= width) {
			deltaX = width / 2 - rf.left - rf.width() / 2;
		}
		if (rf.height() <= height) {
			deltaY = height / 2 - rf.top - rf.height() / 2;
		}

		scaleMatrix.postTranslate(deltaX, deltaY);

	}

	private RectF getRect() {//得到存放图片上下左右位置的矩阵
		RectF rf = new RectF();
		if (d != null) {
			rf.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			scaleMatrix.mapRect(rf);  //得到矩阵中对应的图片的4个方位的矩阵
		}
		return rf;
	}


	private float getCurrentScale() {//得到矩阵当前的缩放比例
		scaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {

	}
}
