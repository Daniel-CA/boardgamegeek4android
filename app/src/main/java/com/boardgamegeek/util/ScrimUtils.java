/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.boardgamegeek.util;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.View;

import com.boardgamegeek.R;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

/**
 * Utility methods for creating prettier gradient scrims. Lovingly borrowed from com.google.android.apps.muzei.util.
 */
public class ScrimUtils {
	private ScrimUtils() {
	}

	/**
	 * Apply a white scrim foreground that's whitest at the bottom.
	 */
	public static void applyWhiteScrim(View view) {
		if (VERSION.SDK_INT < VERSION_CODES.M) return;
		if (view == null) return;
		int color = ContextCompat.getColor(view.getContext(), R.color.white_overlay);
		Drawable drawable = ScrimUtils.makeCubicGradientScrimDrawable(color, 4, Gravity.BOTTOM);
		view.setForeground(drawable);
	}

	/**
	 * Apply a black scrim background that's darkest at the bottom.
	 */
	public static void applyDarkScrim(View view) {
		if (view == null) return;
		int color = ContextCompat.getColor(view.getContext(), R.color.black_overlay);
		Drawable drawable = ScrimUtils.makeCubicGradientScrimDrawable(color, 3, Gravity.BOTTOM);
		ViewCompat.setBackground(view, drawable);
	}

	/**
	 * Creates an approximated cubic gradient using a multi-stop linear gradient. See
	 * <a href="https://plus.google.com/+RomanNurik/posts/2QvHVFWrHZf">this post</a> for more
	 * details.
	 */
	@SuppressLint("RtlHardcoded")
	public static Drawable makeCubicGradientScrimDrawable(int baseColor, int numStops, int gravity) {
		numStops = Math.max(numStops, 2);

		PaintDrawable paintDrawable = new PaintDrawable();
		paintDrawable.setShape(new RectShape());

		final int[] stopColors = new int[numStops];

		int red = Color.red(baseColor);
		int green = Color.green(baseColor);
		int blue = Color.blue(baseColor);
		int alpha = Color.alpha(baseColor);

		for (int i = 0; i < numStops; i++) {
			float x = i * 1f / (numStops - 1);
			float opacity = MathUtils.constrain((float) Math.pow(x, 3), 0, 1);
			stopColors[i] = Color.argb((int) (alpha * opacity), red, green, blue);
		}

		final float x0, x1, y0, y1;
		switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
			case Gravity.LEFT:
				x0 = 1;
				x1 = 0;
				break;
			case Gravity.RIGHT:
				x0 = 0;
				x1 = 1;
				break;
			default:
				x0 = 0;
				x1 = 0;
				break;
		}
		switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
			case Gravity.TOP:
				y0 = 1;
				y1 = 0;
				break;
			case Gravity.BOTTOM:
				y0 = 0;
				y1 = 1;
				break;
			default:
				y0 = 0;
				y1 = 0;
				break;
		}

		paintDrawable.setShaderFactory(new ShapeDrawable.ShaderFactory() {
			@Override
			public Shader resize(int width, int height) {
				return new LinearGradient(
					width * x0,
					height * y0,
					width * x1,
					height * y1,
					stopColors, null,
					Shader.TileMode.CLAMP);
			}
		});

		return paintDrawable;
	}
}