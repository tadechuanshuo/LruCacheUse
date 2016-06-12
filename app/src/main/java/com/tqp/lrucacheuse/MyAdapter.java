package com.tqp.lrucacheuse;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class MyAdapter extends BaseAdapter {
	private Context context = null;
	private LruMemoryCache mMemoryCache = null;
	private int[] resIDs = null;

	public MyAdapter(Context context, LruMemoryCache memoryCacher, int[] ids) {
		this.context = context;
		mMemoryCache = memoryCacher;
		resIDs = ids;
	}

	@Override
	public int getCount() {
		return resIDs.length;
	}

	@Override
	public Object getItem(int arg0) {
		return resIDs[arg0];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			ImageView imageView = new ImageView(context);
			imageView.setLayoutParams(new ViewGroup.LayoutParams((int) (0.4 * UtilScreen.getScreenWidth(context)), (int) (UtilScreen.getScreenWidth(context)*0.4)));
//			imageView.setLayoutParams((int) UtilScreen.getScreenWidth(context), (int) UtilScreen.getScreenHeight(context));
			convertView = imageView;
		}

		// 交替执行下面两句代码，看看显示结果。
//		((ImageView) convertView).setImageResource(resIDs[position]);
		loadBitmap(resIDs[position], (ImageView)convertView);

		return convertView;
	}
//	http://7xq4hn.com2.z0.glb.qiniucdn.com/20160603185905-1875832977.png
	public void loadBitmap(int resId, ImageView imageView) {
		final String imageKey = String.valueOf(resId);
		final Bitmap bitmap = mMemoryCache.getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.mipmap.ic_launcher);
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			task.execute(resId);
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		ImageView mImageView;

		public BitmapWorkerTask(ImageView imageView) {
			mImageView = imageView;
		}

		// 在后台加载图片。
		@Override
		protected Bitmap doInBackground(Integer... params) {
			final Bitmap bitmap = decodeSampledBitmapFromResource(context.getResources(), params[0], 100, 100);
			mMemoryCache.addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
			Log.i("TAG_SYL", "====memoryCache sum: " + mMemoryCache.size() / 1024);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			mImageView.setImageBitmap(result);
			super.onPostExecute(result);
		}
	}


}
