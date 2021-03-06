/**
Copyright (c) 2011-present - Luu Gia Thuy

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
 */

package sdk.android.downloader;

import java.net.HttpURLConnection;

public class HttpDownloader extends Downloadable {

	public HttpDownloader(Part part, int numConnections, String filename) {
		super(part, numConnections, filename);
		// download();
	}

	private void error() {
		System.out.println("ERROR");
		setState(State.ERROR);
	}

	/**
	 * 获取文件大小，根据大小进行分发下载线程
	 */
	@Override
	public void run() {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) mPart.mURL.openConnection();
			conn.setConnectTimeout(10000);
			conn.connect();

			// Make sure the response code is in the 200 range.
			if (conn.getResponseCode() / 100 != 2) {
				error();
			}

			// Check for valid content length.
			int contentLength = conn.getContentLength();
			if (contentLength < 1) {
				error();
			}

			if (mPart.mFileSize == -1) {
				mPart.mFileSize = contentLength;
				stateChanged();
				System.out.println("File size: " + mPart.mFileSize);
			}

			// if the state is DOWNLOADING (no error) -> start downloading
			if (mState.isLoading()) {
				// check whether we have list of download threads or not, if not
				// -> init download
				if (mListDownloadThread.size() == 0) {
					if (mPart.mFileSize > MIN_DOWNLOAD_SIZE) {
						// downloading size for each thread
						int partSize = Math
								.round(((float) mPart.mFileSize / mNumConnections)
										/ BLOCK_SIZE)
								* BLOCK_SIZE;
						System.out.println("Part size: " + partSize);

						// start/end Byte for each thread
						int startByte = 0;
						int endByte = partSize - 1;
						mPart.mStartByte = startByte;
						mPart.mEndByte = endByte;

						HttpDownloadThread aThread = new HttpDownloadThread(
								this, 1, mPart);
						mListDownloadThread.add(aThread);
						int i = 2;
						while (endByte < mPart.mFileSize) {
							startByte = endByte + 1;
							endByte += partSize;

							mPart = new Part(mPart.mURL, startByte, endByte);
							aThread = new HttpDownloadThread(this, i, mPart);
							mListDownloadThread.add(aThread);
							++i;
						}
					} else {
						mPart.mEndByte = mPart.mFileSize;
						HttpDownloadThread aThread = new HttpDownloadThread(
								this, 1, mPart);
						mListDownloadThread.add(aThread);
					}
				} else { // resume all downloading threads
					for (int i = 0; i < mListDownloadThread.size(); ++i) {
						if (!mListDownloadThread.get(i).isFinished())
							mListDownloadThread.get(i).download();
					}
				}

				// waiting for all threads to complete
				for (int i = 0; i < mListDownloadThread.size(); ++i) {
					mListDownloadThread.get(i).waitFinish();
				}

				// check the current state again
				if (mState.isLoading()) {
					setState(State.COMPLETED);
				}
			}
		} catch (Exception e) {
			error();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}

}
