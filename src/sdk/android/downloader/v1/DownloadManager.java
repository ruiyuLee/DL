/**
 * 
 */
package sdk.android.downloader.v1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Observable;

import sdk.android.downloader.v1.DownloadManager.Downloader.OnDownloadListener;
import sdk.android.util.FileSizer;

/**
 * @author liruiyu E-mail:allnet@live.cn
 * @version 创建时间：2014-6-25 类说明
 */
public class DownloadManager {

	public static void download(List<Point> list, DataOuter dataOuter,
			OnDownloadListener listener) {
		Downloader downloader = new Downloader(list, dataOuter, listener);
	}

	public static class Point {
		public URL URL;
		public int start;
		public int end;

		public Point(URL url, int startByte, int endByte) {
			this.URL = url;
			this.start = startByte;
			this.end = endByte;
		}

		public boolean hasRange() {
			return end == 0;
		}
	}

	public static abstract class Downloadable extends Observable implements
			Runnable {
		/** The state of the download */
		protected State mState;
		protected static final int BLOCK_SIZE = 4096;
		protected static final int BUFFER_SIZE = 4096;

		/** 下载状态 */
		public static enum State {
			// These are the status names.
			DOWNLOADING(0, "Downloading"), PAUSED(1, "Paused"), COMPLETED(2,
					"Complete"), CANCELLED(3, "Cancelled"), ERROR(4, "Error");

			public String name;
			public int value;

			State(int value, String name) {
				this.value = value;
				this.name = name;
			}

			public boolean isLoading() {
				return this == DOWNLOADING;
			}
		};

		/**
		 * Pause the downloader
		 */
		public void pause() {
			setState(State.PAUSED);
		}

		/**
		 * Resume the downloader
		 */
		public void resume() {
			setState(State.DOWNLOADING);
			download();
		}

		/**
		 * Start or resume download
		 */
		protected void download() {
			new Thread(this).start();
		}

		/**
		 * Cancel the downloader
		 */
		public void cancel() {
			setState(State.CANCELLED);
		}

		/**
		 * Set the state of the downloader
		 */
		protected void setState(State state) {
			mState = state;
			stateChanged();
		}

		/**
		 * Set the state has changed and notify the observers
		 */
		protected void stateChanged() {
			setChanged();
			notifyObservers();
		}
	}

	public static class Downloader extends Downloadable {
		private OnDownloadListener iListener;
		private DataOuter dataOuter;
		private List<Point> list;

		public Downloader(List<Point> list, DataOuter dataOuter,
				OnDownloadListener listener) {
			iListener = listener;
			this.list = list;
			this.dataOuter = dataOuter;
		}

		void downloaded(int progress) {
			if (iListener != null)
				iListener.onProgress(progress);
		}

		public interface OnDownloadListener {
			public void onStart();

			public void onProgress(int progress);

			public void onStop();
		}

		@Override
		public void run() {
			if (iListener != null)
				iListener.onStart();
			if (list != null) {
				int length = list.size();
				for (int i = 0; i < length; i++) {
					Point p = list.get(i);
					down(p);
				}
			}
		}

		/**
		 * 检查文件下载和写入位置
		 * 
		 * @param conn
		 * @param point
		 *            根据point描述
		 * @throws Exception
		 */
		private void checkRange(HttpURLConnection conn, Point point)
				throws Exception {
			if (point.hasRange()) {// 指定读取范围
				String byteRange = point.start + "-" + point.end;
				conn.setRequestProperty("Range", "bytes=" + byteRange);
				System.out.println("bytes=" + byteRange);
			} else {// 否则对文件追加新数据
				long fileLength = FileSizer.getFileSizes(new File(
						dataOuter.outputFile));
				point.start = (int) fileLength == -1 ? 0 : (int) fileLength;
			}
		}

		private void down(Point point) {
			BufferedInputStream in = null;
			RandomAccessFile raf = null;
			try {
				HttpURLConnection conn = (HttpURLConnection) point.URL
						.openConnection();
				conn.connect();
				checkRange(conn, point);
				// Make sure the response code is in the 200 range.
				if (conn.getResponseCode() / 100 != 2) {
					setState(State.ERROR);
				}
				in = new BufferedInputStream(conn.getInputStream());
				// open the output file and seek to the start location
				raf = new RandomAccessFile(dataOuter.outputFile, "rw");
				raf.seek(point.start);

				byte data[] = new byte[BUFFER_SIZE];
				int numRead;
				while ((mState.isLoading())
						&& ((numRead = in.read(data, 0, BUFFER_SIZE)) != -1)) {
					raf.write(data, 0, numRead);
					// increase the startByte for resume later
					point.start += numRead;
					// increase the downloaded size
					downloaded(numRead);
				}

				if (mState.isLoading()) {
					setState(State.COMPLETED);
					if (iListener != null)
						iListener.onStop();
				}
			} catch (Exception e) {
				setState(State.ERROR);
			} finally {
				try {
					if (raf != null)
						raf.close();
					if (in != null)
						in.close();
				} catch (IOException e) {
				}
			}

			System.out.println("End thread ");

		}
	}

}
