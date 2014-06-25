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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import sdk.android.downloader.Downloadable.Part;

public class DownloadManager {

	private static DownloadManager sInstance = null;

	// Constant variables
	private static final int DEFAULT_NUM_CONN_PER_DOWNLOAD = 4;
	public static final String DEFAULT_OUTPUT_FOLDER = "";

	// Member variables
	private int mNumConnPerDownload;
	private ArrayList<Downloadable> mDownloadList;

	/** Protected constructor */
	protected DownloadManager() {
		mNumConnPerDownload = DEFAULT_NUM_CONN_PER_DOWNLOAD;
		mDownloadList = new ArrayList<Downloadable>();
	}

	/**
	 * Get the max. number of connections per download
	 */
	public int getNumConnPerDownload() {
		return mNumConnPerDownload;
	}

	/**
	 * Set the max number of connections per download
	 */
	public void SetNumConnPerDownload(int value) {
		mNumConnPerDownload = value;
	}

	/**
	 * Get the downloader object in the list
	 * 
	 * @param index
	 * @return
	 */
	public Downloadable getDownload(int index) {
		return mDownloadList.get(index);
	}

	public void removeDownload(int index) {
		mDownloadList.remove(index);
	}

	/**
	 * Get the download list
	 * 
	 * @return
	 */
	public ArrayList<Downloadable> getDownloadList() {
		return mDownloadList;
	}

	public Downloadable createDownload(URL verifiedURL, String outputFile) {
		Part part = new Part(verifiedURL, 0, 0);
		HttpDownloader fd = new HttpDownloader(part, mNumConnPerDownload,
				outputFile);
		fd.download();
		mDownloadList.add(fd);
		return fd;
	}

	public static void download(List<Part> parts, String outputFile, Observer o) {
		HttpDownloader downloader = new HttpDownloader(parts.get(0), 1,
				outputFile);
		downloader.addObserver(o);
		getInstance().mDownloadList.add(downloader);
		HttpDownloadThread d = new HttpDownloadThread(downloader, 1, parts);
	}

	public static long currentLength(String filename) {
		File file = new File(filename);
		if (file.exists()) {
			return file.length();
		}
		return -1;
	}

	/**
	 * Get the unique instance of this class
	 * 
	 * @return the instance of this class
	 */
	public static DownloadManager getInstance() {
		if (sInstance == null)
			sInstance = new DownloadManager();

		return sInstance;
	}

	/**
	 * Verify whether an URL is valid
	 * 
	 * @param fileURL
	 * @return the verified URL, null if invalid
	 */
	public static URL verifyURL(String fileURL) {
		// Only allow HTTP URLs.
		if (!fileURL.toLowerCase().startsWith("http://"))
			return null;

		// Verify format of URL.
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(fileURL);
		} catch (Exception e) {
			return null;
		}

		// Make sure URL specifies a file.
		if (verifiedUrl.getFile().length() < 2)
			return null;

		return verifiedUrl;
	}

}
