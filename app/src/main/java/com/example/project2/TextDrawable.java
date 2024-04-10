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

    // Method to generate Bitmap from text
    public static Bitmap generateBitmap(Context context, String text, Bitmap backgroundImage, int screenWidth, int screenHeight) {
        // Calculate initial text size based on desired height
        Typeface customFont = ResourcesCompat.getFont(context, R.font.oswald_light);
        Typeface headingFont = ResourcesCompat.getFont(context, R.font.oswald_regular);
        float textSize = 40; // Initial text size
        Paint textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(customFont);

        // Calculate desired width and height for the top half of the screen
        int desiredWidth = screenWidth;
        int desiredHeight = screenHeight / 2;

        // Split the text into multiple lines
        String[] lines = text.split("\n");

        // Calculate the total height required for all the text lines
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

        // Adjust text size to fit within desired width and height
        while (totalTextHeight > desiredHeight || textPaint.measureText(lines[0]) > desiredWidth) {
            textSize--; // Decrease text size
            textPaint.setTextSize(textSize);

            // Recalculate totalTextHeight
            totalTextHeight = 0;
            for (String line : lines) {
                textPaint.getTextBounds(line, 0, line.length(), bounds);
                totalTextHeight += bounds.height();
            }
        }

        // Create a new Bitmap with adjusted size
        Bitmap bitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Fill the canvas with light pink color
        if (backgroundImage != null) {
            int imageX = (bitmap.getWidth() - backgroundImage.getWidth()) / 2;
            int imageY = (bitmap.getHeight() - backgroundImage.getHeight()) / 2;
            canvas.drawBitmap(backgroundImage, imageX, imageY, null);
        }

        // Draw text onto the canvas
        /*float textX = 10; // Adjust the horizontal position of the text
        float textY = bounds.height() + 50;  // Start drawing text from the top of the canvas
        for (String line : lines) {
            canvas.drawText(line, textX, textY, textPaint);
            textY += bounds.height(); // Move to the next line
        }*/
        float botalTextHeight = bounds.height() * lines.length; // Calculate total text height
        float textY = (desiredHeight - botalTextHeight) / 5 + bounds.height(); // Center vertically
        int counter = 0;
        for (String line : lines) {
            float lineWidth = textPaint.measureText(line);
            float textX = (desiredWidth - lineWidth) / 2; // Center horizontally
            if (counter == 0 || counter == 5 || counter == 12) {
                textPaint.setTypeface(headingFont);
                canvas.drawText(line, textX, textY, textPaint);
            } else {
                textPaint.setTypeface(customFont);
                canvas.drawText(line, textX, textY, textPaint);

            }

            textY += bounds.height(); // Move to the next line
            counter++;
        }

        return bitmap;
    }
}

