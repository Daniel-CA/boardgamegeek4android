package com.boardgamegeek.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.widget.ImageView;

import com.boardgamegeek.R;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Loading images? This is your huckleberry.
 */
public class ImageUtils {
	private static final String IMAGE_URL_PREFIX = "https://cf.geekdo-images.com/images/pic";
	//public static final String SUFFIX_SMALL_THUMBNAIL = "_mt";
	public static final String SUFFIX_THUMBNAIL = "_t";
	//private static final String SUFFIX_SQUARE = "_sq";
	public static final String SUFFIX_SMALL = "_t";
	public static final String SUFFIX_MEDIUM = "_md";
	//private static final String SUFFIX_LARGE = "_lg";

	private ImageUtils() {
	}

	/**
	 * Create a URL for a thumbnail image as a JPG.
	 */
	public static String createThumbnailJpgUrl(int imageId) {
		return IMAGE_URL_PREFIX + imageId + SUFFIX_SMALL + ".jpg";
	}

	/**
	 * Create a URL for a thumbnail image as a PNG.
	 */
	public static String createThumbnailPngUrl(int imageId) {
		return IMAGE_URL_PREFIX + imageId + SUFFIX_SMALL + ".png";
	}

	/**
	 * Loads an image into the {@link android.widget.ImageView} by attempting various sizes and image formats. Applies
	 * fit/center crop and will load a {@link android.support.v7.graphics.Palette}.
	 */
	public static void safelyLoadImage(ImageView imageView, int imageId, Callback callback) {
		Queue<String> imageUrls = new LinkedList<>();
		String imageUrl = IMAGE_URL_PREFIX + imageId + ".jpg";
		imageUrls.add(appendImageUrl(imageUrl, SUFFIX_MEDIUM));
		imageUrls.add(appendImageUrl(imageUrl, SUFFIX_SMALL));
		imageUrls.add(imageUrl);
		imageUrl = IMAGE_URL_PREFIX + imageId + ".png";
		imageUrls.add(appendImageUrl(imageUrl, SUFFIX_MEDIUM));
		imageUrls.add(appendImageUrl(imageUrl, SUFFIX_SMALL));
		imageUrls.add(imageUrl);
		safelyLoadImage(imageView, imageUrls, callback);
	}

	/**
	 * Loads an image into the {@link android.widget.ImageView} by attempting various sizes. Applies fit/center crop.
	 */
	public static void safelyLoadImage(ImageView imageView, String imageUrl) {
		safelyLoadImage(imageView, imageUrl, null);
	}

	/**
	 * Loads an image into the {@link android.widget.ImageView} by attempting various sizes. Applies fit/center crop and
	 * will load a {@link android.support.v7.graphics.Palette}.
	 */
	public static void safelyLoadImage(ImageView imageView, String imageUrl, Callback callback) {
		Queue<String> imageUrls = new LinkedList<>();
		imageUrls.add(appendImageUrl(imageUrl, SUFFIX_MEDIUM));
		imageUrls.add(appendImageUrl(imageUrl, SUFFIX_SMALL));
		imageUrls.add(imageUrl);
		safelyLoadImage(imageView, imageUrls, callback);
	}

	/**
	 * Loads an image into the {@link android.widget.ImageView} by attempting each URL in the {@link java.util.Queue}
	 * until one is successful. Applies fit/center crop and will load a {@link android.support.v7.graphics.Palette}.
	 */
	private static void safelyLoadImage(final ImageView imageView,
										final Queue<String> imageUrls,
										final Callback callback) {
		String savedUrl = (String) imageView.getTag(R.id.url);
		String url = null;
		if (!TextUtils.isEmpty(savedUrl)) {
			if (imageUrls.contains(savedUrl)) {
				url = savedUrl;
			} else {
				imageView.setTag(R.id.url, null);
			}
		}
		if (TextUtils.isEmpty(url)) {
			url = imageUrls.poll();
		}
		if (TextUtils.isEmpty(url)) {
			if (callback != null) callback.onFailedImageLoad();
			return;
		}
		final String imageUrl = url;
		Picasso
			.with(imageView.getContext())
			.load(HttpUtils.ensureScheme(imageUrl))
			.transform(PaletteTransformation.instance())
			.into(imageView, new com.squareup.picasso.Callback() {
				@Override
				public void onSuccess() {
					imageView.setTag(R.id.url, imageUrl);
					if (callback != null) {
						Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
						Palette palette = PaletteTransformation.getPalette(bitmap);
						callback.onSuccessfulImageLoad(palette);
					}
				}

				@Override
				public void onError() {
					safelyLoadImage(imageView, imageUrls, callback);
				}
			});
	}

	/**
	 * Append a suffix to an image URL. Assumes the URL has no suffix (but may have an extension or not.
	 */
	public static String appendImageUrl(String imageUrl, String suffix) {
		if (TextUtils.isEmpty(imageUrl)) {
			return "";
		}
		if (TextUtils.isEmpty(suffix)) {
			return imageUrl;
		}
		int dot = imageUrl.lastIndexOf('.');
		if (dot == -1) {
			return imageUrl + suffix;
		} else {
			return imageUrl.substring(0, dot) + suffix + imageUrl.substring(dot, imageUrl.length());
		}
	}

	/**
	 * Call back from loading an image.
	 */
	public interface Callback {
		void onSuccessfulImageLoad(Palette palette);

		void onFailedImageLoad();
	}

	public static void loadThumbnail(int imageId, ImageView target) {
		Queue<String> queue = new LinkedList<>();
		queue.add(ImageUtils.createThumbnailJpgUrl(imageId));
		queue.add(ImageUtils.createThumbnailPngUrl(imageId));
		safelyLoadThumbnail(target, queue);
	}

	public static void loadThumbnail(String path, ImageView target) {
		Queue<String> queue = new LinkedList<>();
		queue.add(path);
		safelyLoadThumbnail(target, queue);
	}

	private static void safelyLoadThumbnail(final ImageView imageView, final Queue<String> imageUrls) {
		final String imageUrl = imageUrls.poll();
		if (TextUtils.isEmpty(imageUrl)) {
			return;
		}
		Picasso.with(imageView.getContext())
			.load(HttpUtils.ensureScheme(imageUrl))
			.placeholder(R.drawable.thumbnail_image_empty)
			.error(R.drawable.thumbnail_image_empty)
			.resizeDimen(R.dimen.thumbnail_list_size, R.dimen.thumbnail_list_size)
			.centerCrop()
			.into(imageView, new com.squareup.picasso.Callback() {
				@Override
				public void onSuccess() {
				}

				@Override
				public void onError() {
					safelyLoadThumbnail(imageView, imageUrls);
				}
			});
	}
}
