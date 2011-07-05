package org.yavdr.yadroid.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;

public class BitmapText extends BitmapDrawable {
	private String name;
	private Bitmap bitmap;

	public BitmapText(Context ctx, String strName, int bitmapId) {

		// TODO Auto-generated constructor stub
		super(BitmapFactory.decodeResource(ctx.getResources(), bitmapId));
		bitmap = BitmapFactory.decodeResource(ctx.getResources(), bitmapId);
		name = strName;
		this.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
	}

	@Override
	public void draw(Canvas arg0) {
		// TODO Auto-generated method stub
		Paint textPaint = new Paint();
		arg0.drawBitmap(bitmap, this.getBounds().left, this.getBounds().top,
				textPaint);
		textPaint.setTextSize(12);
		textPaint.setTypeface(Typeface.DEFAULT);
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Style.FILL);
		arg0.drawText(name, 10, 10, textPaint);

	}
}