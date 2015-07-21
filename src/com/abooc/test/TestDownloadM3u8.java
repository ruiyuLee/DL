/**
 * 
 */
package com.abooc.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import sdk.android.downloader.v1.DataOuter;
import sdk.android.downloader.v1.DownloadManager;
import sdk.android.downloader.v1.DownloadManager.Downloader.OnDownloadListener;
import sdk.android.downloader.v1.DownloadManager.Point;
import sdk.android.util.Log;

/**
 * @author liruiyu E-mail:allnet@live.cn
 * @version 创建时间：2014-6-25 类说明
 */
public class TestDownloadM3u8 {

	public static void main(String[] args) {

	}

	static void down() {
		try {
			String outputFile = "56732_53864_449154.jpg";
			URL url = new URL(M3u8FileDownloader.image_url);
			Point Point = new Point(url, 0, 256287 / 2);// 下载前一半
			Point Point2 = new Point(url, 256287 / 2, 256287);// 下载后一半
			List<Point> list = new ArrayList<Point>(2);
			list.add(Point);
			list.add(Point2);
			OnDownloadListener iOnDownloadListener = new OnDownloadListener() {
				@Override
				public void onStart() {
					Log.d("onStart()");
				}

				@Override
				public void onProgress(int progress) {
					Log.d("onProgress()" + progress);
				}

				@Override
				public void onStop() {
					Log.d("onStop()");
				}
			};

			DataOuter dataOuter = new DataOuter("") {
				@Override
				public void onLoaded(Point point, byte[] data) {

				}
			};
			DownloadManager.download(list, dataOuter, iOnDownloadListener);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
