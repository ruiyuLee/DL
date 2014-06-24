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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import sdk.android.downloader.Downloadable.Part;
import net.chilicat.m3u8.Element;
import net.chilicat.m3u8.Playlist;

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

	public static void downloadPart(List<Part> parts, String outputFile) {
		HttpDownloader downloader = new HttpDownloader(parts.get(0), 1,
				outputFile);
		downloader.downloadParts(parts);
	}

	public static void downloadM3u8(String m3u8Url, String outputFile) {
		if (m3u8Url.contains(".m3u8")) {
			System.out.println(".m3u8");
			List<Element> list = testParseM3u8(m3u8Url);
			List<Part> parts = testToPart(list);
			DownloadManager.downloadPart(parts, "souhu2.mp4");
		} else {
			System.out.println("不是.m3u8");
			try {
				Part part = new Part(new URL(m3u8Url), 0, 0);
				List<Part> list = new ArrayList<Part>(1);
				list.add(part);
				DownloadManager.downloadPart(list, "souhu2.mp4");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	static List<Element> testParseM3u8(String m3u8Url) {
		InputStream in = null;
		try {
			URI uri = URI.create(m3u8Url);
			in = uri.toURL().openStream();
			Playlist playlist = Playlist.parse(in);

			List<Element> list = playlist.getElements();
			// StringBuffer buffer = new StringBuffer();
			// for (int i = 0; i < list.size(); i++) {
			// buffer.append(list.get(i).toString()).append("\n");
			// }
			//
			// System.out.println(buffer.toString());
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	static List<Part> testToPart(List<Element> list) {
		int i = 0;
		int length = 2;
		List<Part> listPort = new ArrayList<Part>();
		try {
			for (; i < length; i++) {
				Element e = list.get(i);
				Part part = new Part(e.getURI().toURL(), 0, 0);
				listPort.add(part);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return listPort;
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
