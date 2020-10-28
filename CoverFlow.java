package com.chenxu.coverflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class CoverFlow extends ViewGroup {
	private static final int NUMBER_PER_PAGE = 5;
	private static final float ROTATE_DEGREE = 30;
	private static final float VELOCITY_THRESHOLD = 500;
	private static final int SCROLL_DURATION = 1000;
	private List<Integer> imageIdList;
	private ArrayList<Bitmap> bitmapList;
	private ArrayList<ImageView> imageViewList;
	private int width;
	private int height;
	private int itemWidth;
	private int middleIndex;
	private Scroller scroller;
	private int padNumber;
	private float lastRawX;
	private int originalScrollX;

	private VelocityTracker velocityTracker;
	private boolean canTouch = true;
	private CoverFlowListener listener;
	private GestureDetector gestureDetector;

	public CoverFlow(Context context, List<Integer> imageIdList,
                     CoverFlowListener listener) {
		super(context);
		// TODO Auto-generated constructor stub
		this.imageIdList = imageIdList;
		this.listener = listener;
		bitmapList = new ArrayList<Bitmap>();
		imageViewList = new ArrayList<ImageView>();
		middleIndex = 0;
		padNumber = NUMBER_PER_PAGE / 2;
		scroller = new Scroller(getContext());

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inSampleSize = 4;
		for (int i = 0; i < imageIdList.size(); i++) {
			int imageId = imageIdList.get(i);
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					imageId, options);
			bitmapList.add(bitmap);
			ImageView imageView = new ImageView(getContext());
			imageView.setPadding(6, 6, 6, 6);
			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			imageView.setImageBitmap(bitmap);
			LayoutParams imageViewLayoutParams = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			imageView.setLayoutParams(imageViewLayoutParams);
			imageViewList.add(imageView);
			addView(imageView);
		}
		for (int i = 0; i < padNumber; i++) {
			ImageView imageView = new ImageView(getContext());
			imageView.setPadding(6, 6, 6, 6);
			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			imageView.setImageBitmap(null);
			bitmapList.add(null);
			LayoutParams imageViewLayoutParams = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			imageView.setLayoutParams(imageViewLayoutParams);
			imageViewList.add(imageView);
			addView(imageView);
		}

		gestureDetector = new GestureDetector(gestureListener);
	}

	public CoverFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CoverFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public Bitmap getReflectedBitmap(Bitmap bitmap) {
//		LogUtil.i("chenxu", "bitmap's width:"+bitmap.getWidth()+" height:"+bitmap.getHeight()+" totalHeight:"+height);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, itemWidth, height*2/3, true);
		Bitmap resultBitmap = Bitmap.createBitmap(itemWidth, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(resultBitmap);
		canvas.drawBitmap(scaledBitmap, null, new Rect(0, 0, itemWidth, height*2/3), null);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		LinearGradient shader = new LinearGradient(0, height*2/3, 0, height, 0x88ffffff, 0xffffffff, TileMode.CLAMP);
		paint.setShader(shader);
		paint.setAlpha(180);
		Matrix matrix = new Matrix();
		matrix.postScale(1, -1);
		Bitmap reflectionBitmap = Bitmap.createBitmap(scaledBitmap, 0, height*2/3*2/3, itemWidth, height*2/3*1/3, matrix, true);
		canvas.drawBitmap(reflectionBitmap, 0, height*2/3, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		Rect r = new Rect(0, height*2/3, 0, height);
		canvas.drawRect(r, paint);
		return resultBitmap;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		if (changed) {
			int left = padNumber * itemWidth;
			int top = 0;
			int right = left + itemWidth;
			int bottom = height;
			for (int i = 0; i < getChildCount(); i++) {
				ImageView imageView = (ImageView) getChildAt(i);
				LayoutParams imageViewLayoutParams = imageView
						.getLayoutParams();
				imageViewLayoutParams.width = itemWidth;
				imageViewLayoutParams.height = height;
//				Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
				Bitmap bitmap = bitmapList.get(i);
				if (bitmap!=null) {
					Bitmap reflectedBitmap = getReflectedBitmap(bitmap);
					imageView.setImageBitmap(reflectedBitmap);
				}
				imageView.setLayoutParams(imageViewLayoutParams);
				left = itemWidth * (i + padNumber);
				right = left + itemWidth;
				imageView.layout(left, top, right, bottom);
				if (i < middleIndex) {
					imageView.setRotationY(ROTATE_DEGREE);
				} else if (i == middleIndex) {
					imageView.setRotationY(0);
				} else {
					imageView.setRotationY(-ROTATE_DEGREE);
				}
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		if (w != 0 && h != 0) {
			width = w;
			height = h;
			itemWidth = width / NUMBER_PER_PAGE;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (gestureDetector.onTouchEvent(ev)) {
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		// if (!scroller.isFinished()) {
		// scroller.abortAnimation();
		// // return true;
		// }
		// if (!canTouch) {
		// return true;
		// }
		float rawX = event.getRawX();
		float rawY = event.getRawY();
		initVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			originalScrollX = getScrollX();
			lastRawX = rawX;
			return true;
		case MotionEvent.ACTION_MOVE:
			float xOffset = -(rawX - lastRawX);
			moveByXOffset((int) xOffset);
			lastRawX = rawX;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			int velocityX = (int) getVelocityX();
			if (velocityX > VELOCITY_THRESHOLD) {
				scrollRight(velocityX);
			} else if (velocityX < -VELOCITY_THRESHOLD) {
				scrollLeft(velocityX);
			} else {
				scrollBack();
			}
			recycleVelocityTracker();
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		public boolean onSingleTapUp(MotionEvent e) {
//			LogUtil.i("chenxu", "onSingleTapUp e.getX():" + e.getX()
//					+ " e.getRawX():" + e.getRawX());
			int index = (int) (e.getX()+getScrollX() -padNumber*itemWidth)
					/ itemWidth;
			LogUtil.i("chenxu", "calculated index:"+index);
			if (isIndexValid(index)) {
				if (listener != null) {
					listener.coverFlowDidClick(index);
				}
				return true;
			} else {
				return false;
			}
		};
	};

	public void scrollRight(int velocityX) {
		int processedVelocityX = velocityX / 2;
		int index = (int) (getScrollX() + itemWidth / 2) / itemWidth;
		int currentScrollX = padNumber * itemWidth + getScrollX() + itemWidth
				/ 2;
		int leftScrollX = padNumber * itemWidth + Math.max(0, index)
				* itemWidth + itemWidth / 2;
		int rightScrollX = padNumber * itemWidth
				+ Math.min(imageViewList.size() - 1, index + 1) * itemWidth
				+ itemWidth / 2;
		int dx = 0;
		int leftAbs = Math.abs(currentScrollX - leftScrollX);
		int rightAbs = Math.abs(currentScrollX - rightScrollX);
		if (leftAbs >= rightAbs) {
			dx = -(rightScrollX - currentScrollX) - processedVelocityX
					/ itemWidth * itemWidth;
		} else {
			dx = leftScrollX - currentScrollX - processedVelocityX / itemWidth
					* itemWidth;
		}
		while (dx < -getScrollX()) {
			dx += itemWidth;
		}
		int duration = (int) Math.abs(500.0f/itemWidth*dx);
		LogUtil.i("chenxu", "scrollRight dx:" + dx+" duration:"+duration);
		scroller.startScroll(getScrollX(), 0, dx, 0, duration);
		invalidate();
	}

	public void scrollLeft(int velocityX) {
		int processedVelocityX = velocityX / 2;
		int index = (int) (getScrollX() + itemWidth / 2) / itemWidth;
		int currentScrollX = padNumber * itemWidth + itemWidth / 2
				+ getScrollX();
		int leftScrollX = padNumber * itemWidth + itemWidth / 2
				+ Math.max(0, index) * itemWidth;
		int rightScrollX = padNumber * itemWidth + itemWidth / 2
				+ Math.min(imageViewList.size() - 1, index + 1) * itemWidth;
		int dx = 0;
		int leftAbs = Math.abs(currentScrollX - leftScrollX);
		int rightAbs = Math.abs(currentScrollX - rightScrollX);
		if (leftAbs >= rightAbs) {
			dx = -processedVelocityX / itemWidth * itemWidth
					- (rightScrollX - currentScrollX);
		} else {
			dx = -processedVelocityX / itemWidth * itemWidth
					+ (currentScrollX - leftScrollX);
		}
		while (dx > (itemWidth * (imageIdList.size() - 1) - getScrollX())) {
			dx -= itemWidth;
		}
		int duration = (int) Math.abs(500.0f/itemWidth*dx);
		LogUtil.i("chenxu", "scrollLeft dx:" + dx+" duration:"+duration);
		scroller.startScroll(getScrollX(), 0, dx, 0, duration);
		invalidate();
	}

	public void scrollBack() {
		int index = (int) (getScrollX() + itemWidth / 2) / itemWidth;
		int currentScrollX = padNumber * itemWidth + itemWidth / 2
				+ getScrollX();
		int leftScrollX = padNumber * itemWidth + itemWidth / 2
				+ Math.max(0, index) * itemWidth;
		int rightScrollX = padNumber * itemWidth + itemWidth / 2
				+ Math.min(imageViewList.size() - 1, index + 1) * itemWidth;
		int dx = 0;
		int leftAbs = Math.abs(currentScrollX - leftScrollX);
		int rightAbs = Math.abs(currentScrollX - rightScrollX);
		if (leftAbs >= rightAbs) {
			dx = -(rightScrollX - currentScrollX);
		} else {
			dx = currentScrollX - leftScrollX;
		}
		int duration = (int)Math.abs(500.0f/itemWidth*dx);
		LogUtil.i("chenxu", "scrollBack dx:" + dx+" duration:"+duration);
		scroller.startScroll(getScrollX(), 0, dx, 0, duration);
		invalidate();
	}

	public void moveByXOffset(int xOffset) {
		int scrollX = getScrollX();
		if (scrollX <= 0 && xOffset < 0) {
			return;
		}
		if (scrollX >= (imageIdList.size() - 1) * itemWidth && xOffset > 0) {
			return;
		}
		if (xOffset<0) {
			if (scrollX+xOffset>=0) {
			} else {
				xOffset=-scrollX;
			}
		} else {
			if (scrollX+xOffset<=(imageIdList.size()-1)*itemWidth) {
				
			} else {
				xOffset=(imageIdList.size()-1)*itemWidth-scrollX;
			}
		}
		scrollBy(xOffset, 0);
		adjust();

	}

	public void adjust() {
		middleIndex = (int) (getScrollX() + itemWidth / 2) / itemWidth;
		// int left = padNumber * itemWidth + getScrollX();
		// int top = 0;
		// int right = left + itemWidth;
		// int bottom = height;
		for (int i = 0; i < getChildCount(); i++) {
			ImageView imageView = (ImageView) getChildAt(i);
			// LayoutParams imageViewLayoutParams = imageView.getLayoutParams();
			// imageViewLayoutParams.width = itemWidth;
			// imageViewLayoutParams.height = height;
			// imageView.setLayoutParams(imageViewLayoutParams);
			// left += i * itemWidth;
			// // LogUtil.i("chenxu", "adjust left:"+left);
			// right = left + itemWidth;
			// imageView.layout(left, top, right, bottom);
			if (i < middleIndex) {
				imageView.setRotationY(ROTATE_DEGREE);
			} else if (i == middleIndex) {
				imageView.setRotationY(0);
			} else {
				imageView.setRotationY(-ROTATE_DEGREE);
			}
		}
	}
	
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			adjust();
			postInvalidate();
			canTouch = false;
		} else {
			canTouch = true;
		}
	}

	public void initVelocityTracker(MotionEvent event) {
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);
	}

	public float getVelocityX() {
		velocityTracker.computeCurrentVelocity(1000);
		return velocityTracker.getXVelocity();
	}

	public void recycleVelocityTracker() {
		if (velocityTracker != null) {
			velocityTracker.recycle();
		}
		velocityTracker = null;
	}

	public boolean isIndexValid(int index) {
		return index >= 0 && index < imageIdList.size();
	}

	public interface CoverFlowListener {
		public void coverFlowDidClick(int index);
	}
}