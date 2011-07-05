/*
 * Copyright (C) 2010 Neil Davies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code is base on the Android Gallery widget and was Created 
 * by Neil Davies neild001 'at' gmail dot com to be a Coverflow widget
 * 
 * @author Neil Davies
 */
package org.yavdr.yadroid.ui.coverflow;

import org.yavdr.yadroid.R;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

public class CoverFlow extends Gallery {

	public static final String TAG = CoverFlow.class.toString();

	/**
	 * Graphics Camera used for transforming the matrix of ImageViews
	 */
	private Camera mCamera = new Camera();

	/**
	 * The maximum angle the Child ImageView will be rotated by
	 */
	private int mMaxRotationAngle = 60;

	/**
	 * The maximum zoom on the centre Child
	 */
	private int mMaxZoom = -120;

	/**
	 * The Centre of the Coverflow
	 */
	private int mCoveflowCenter;
	private int zoomFactor;
	private int mWidth;
	private float mScaleFactor;
	private int mVirtualWith = 320;
	private int mVirtualCenter;

	private boolean isInit = false;

	private int getZoomAmount(int pos) {

		pos = (int) (mScaleFactor * pos);
		if (pos > mVirtualCenter) {
			return zoomFactor * (mVirtualWith - pos);
		}
		return (zoomFactor * pos);
	}

	private void init() {
		try {
			if (isInit)
				return;
			mWidth = (getWidth() - getPaddingLeft() - getPaddingRight());
			// calculate relative position
			mScaleFactor = ((float) mVirtualWith / mWidth);

			mVirtualCenter = mVirtualWith / 2;
			zoomFactor = -1;
			isInit = true;
		} catch (Exception e) {
			Log.v(TAG, "Init", e);
		}
	}

	public CoverFlow(Context context) {
		super(context);
		this.setStaticTransformationsEnabled(true);

	}

	public CoverFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	public CoverFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
	}

	/**
	 * Get the max rotational angle of the image
	 * 
	 * @return the mMaxRotationAngle
	 */
	public int getMaxRotationAngle() {
		return mMaxRotationAngle;
	}

	/**
	 * Set the max rotational angle of each image
	 * 
	 * @param maxRotationAngle
	 *            the mMaxRotationAngle to set
	 */
	public void setMaxRotationAngle(int maxRotationAngle) {
		mMaxRotationAngle = maxRotationAngle;
	}

	/**
	 * Get the Max zoom of the centre image
	 * 
	 * @return the mMaxZoom
	 */
	public int getMaxZoom() {
		return mMaxZoom;
	}

	/**
	 * Set the max zoom of the centre image
	 * 
	 * @param maxZoom
	 *            the mMaxZoom to set
	 */
	public void setMaxZoom(int maxZoom) {
		mMaxZoom = maxZoom;
	}

	/**
	 * Get the Centre of the Coverflow
	 * 
	 * @return The centre of this Coverflow.
	 */
	private int getCenterOfCoverflow() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	/**
	 * Get the Centre of the View
	 * 
	 * @return The centre of the given view.
	 */
	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #setStaticTransformationsEnabled(boolean)
	 */
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		final int childCenter = getCenterOfView(child);
		final int childWidth = child.getWidth();

		init();

		int rotationAngle = 0;

		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		if (childCenter == mCoveflowCenter) {
			transformImageBitmap((ImageView) child, t, 0);
		} else {
			rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
			if (Math.abs(rotationAngle) > mMaxRotationAngle) {
				rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle
						: mMaxRotationAngle;
			}
			transformImageBitmap((ImageView) child, t, rotationAngle);
		}

		return true;

		/*
		 * final int childCenter = getCenterOfView(child); final int childWidth
		 * = child.getWidth(); init(); t.clear();
		 * t.setTransformationType(Transformation.TYPE_MATRIX);
		 * 
		 * if (childCenter > (mCoveflowCenter + (childWidth / 2)) || childCenter
		 * < (mCoveflowCenter - (childWidth / 2))) {
		 * 
		 * transformImageBitmap(child, t, false);
		 * 
		 * } else { transformImageBitmap(child, t, true); } return true;
		 */
	}

	/**
	 * This is called during layout when the size of this view has changed. If
	 * you were just added to the view hierarchy, you're called with the old
	 * values of 0.
	 * 
	 * @param w
	 *            Current width of this view.
	 * @param h
	 *            Current height of this view.
	 * @param oldw
	 *            Old width of this view.
	 * @param oldh
	 *            Old height of this view.
	 */
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfCoverflow();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/**
	 * Transform the Image Bitmap by the Angle passed
	 * 
	 * @param imageView
	 *            ImageView the ImageView whose bitmap we want to rotate
	 * @param t
	 *            transformation
	 * @param rotationAngle
	 *            the Angle by which to rotate the Bitmap
	 */
	private void transformImageBitmap(View child, Transformation t,
			boolean center) {

		mCamera.save();
		ImageView iv = (ImageView) child.findViewById(R.id.image);

		final Matrix imageMatrix = t.getMatrix();
		final int imageHeight = iv.getLayoutParams().height;
		final int imageWidth = iv.getLayoutParams().width;

		mCamera.translate(0.0f, 0.0f, getZoomAmount(getCenterOfView(child)));
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();
	}

	/**
	 * Transform the Image Bitmap by the Angle passed
	 * 
	 * @param imageView
	 *            ImageView the ImageView whose bitmap we want to rotate
	 * @param t
	 *            transformation
	 * @param rotationAngle
	 *            the Angle by which to rotate the Bitmap
	 */
	private void transformImageBitmap(ImageView child, Transformation t,
			int rotationAngle) {
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		final int imageHeight = child.getHeight(); //getLayoutParams().height;		
		final int imageWidth = child.getWidth();// getLayoutParams().width;
		final int rotation = Math.abs(rotationAngle);

		mCamera.translate(0.0f, 0.0f, 100.0f);

		// As the angle of the view gets less, zoom in
		if (rotation < mMaxRotationAngle) {
			float zoomAmount = (float) (mMaxZoom + (rotation * 1.5));
			mCamera.translate(0.0f, 0.0f, zoomAmount);
		}

		mCamera.rotateY(rotationAngle);
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();
	}
}
