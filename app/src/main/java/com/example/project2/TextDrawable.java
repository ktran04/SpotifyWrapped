package com.example.project2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import okhttp3.Callback;

public class TextDrawable {

    public static Bitmap generateBitmap(Context context, String text, Bitmap backgroundImage, int screenWidth, int screenHeight) {
        Typeface customFont = ResourcesCompat.getFont(context, R.font.oswald_light);
        Typeface headingFont = ResourcesCompat.getFont(context, R.font.oswald_regular);
        float textSize = 40;
        Paint textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(customFont);

        int desiredWidth = screenWidth;
        int desiredHeight = screenHeight / 2;

        String[] lines = text.split("\n");

        Rect bounds = new Rect();
        float totalTextHeight = 0;
        int count = 0;
        for (String line : lines) {
            if (count == 0) {
                textPaint.setTypeface(headingFont);
            } else {
                textPaint.setTypeface(customFont);
            }

            textPaint.getTextBounds(line, 0, line.length(), bounds);
            totalTextHeight += bounds.height();
            count++;
        }

        while (totalTextHeight > desiredHeight || textPaint.measureText(lines[0]) > desiredWidth) {
            textSize--;
            textPaint.setTextSize(textSize);

            totalTextHeight = 0;
            for (String line : lines) {
                textPaint.getTextBounds(line, 0, line.length(), bounds);
                totalTextHeight += bounds.height();
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (backgroundImage != null) {
            int imageX = (bitmap.getWidth() - backgroundImage.getWidth()) / 2;
            int imageY = (bitmap.getHeight() - backgroundImage.getHeight()) / 2;
            canvas.drawBitmap(backgroundImage, imageX, imageY, null);
        }

        float botalTextHeight = bounds.height() * lines.length;
        float textY = (desiredHeight - botalTextHeight) / 5 + bounds.height();
        int counter = 0;
        for (String line : lines) {
            float lineWidth = textPaint.measureText(line);
            float textX = (desiredWidth - lineWidth) / 2;
            if (counter == 0 || counter == 5 || counter == 12) {
                textPaint.setTypeface(headingFont);
                canvas.drawText(line, textX, textY, textPaint);
            } else {
                textPaint.setTypeface(customFont);
                canvas.drawText(line, textX, textY, textPaint);

            }

            textY += bounds.height();
            counter++;
        }

        return bitmap;
    }
}

