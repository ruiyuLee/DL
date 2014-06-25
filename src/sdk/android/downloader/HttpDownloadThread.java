package sdk.android.downloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import sdk.android.downloader.Downloadable.Part;
import sdk.android.downloader.Downloadable.State;

/**
 * Thread using Http protocol to download a part of file
 */
public class HttpDownloadThread implements Runnable {
	protected int mThreadID;
	protected Thread mThread;
	private Downloadable iDownloadable;

	public HttpDownloadThread(Downloadable d, int threadID, Part part) {
		builder(d, threadID);
		this.parts = new ArrayList<Part>();
		this.parts.add(part);
	}

	public HttpDownloadThread(Downloadable d, int threadID, List<Part> parts) {
		builder(d, threadID);
		this.parts = parts;
	}

	private void builder(Downloadable d, int threadID) {
		iDownloadable = d;
		mThreadID = threadID;
		download();
	}

	/**
	 * Get whether the thread is finished download the part of file
	 */
	public boolean isFinished() {
		return iDownloadable.mIsFinished;
	}

	/**
	 * Start or resume the download
	 */
	public void download() {
		mThread = new Thread(this);
		mThread.start();
	}

	/**
	 * Waiting for the thread to finish
	 * 
	 * @throws InterruptedException
	 */
	public void waitFinish() throws InterruptedException {
		mThread.join();
	}

	private List<Part> parts;

	@Override
	public void run() {
		if (parts != null) {
			int length = parts.size();
			for (int i = 0; i < length; i++) {
				Part p = parts.get(i);
				down(p);
			}
		}
	}

	private void down(Part part) {
		BufferedInputStream in = null;
		RandomAccessFile raf = null;

		try {
			// open Http connection to URL
			HttpURLConnection conn = (HttpURLConnection) part.mURL
					.openConnection();
			if (part.mEndByte != 0) {
				// set the range of byte to download
				String byteRange = part.mStartByte + "-" + part.mEndByte;
				conn.setRequestProperty("Range", "bytes=" + byteRange);
				System.out.println("bytes=" + byteRange);
			}
			conn.connect();// connect to server

			String Content_Type = conn.getHeaderField("Content-Type");
			System.out.println("Content_Type:" + Content_Type);

			// Make sure the response code is in the 200 range.
			if (conn.getResponseCode() / 100 != 2) {
				iDownloadable.setState(State.ERROR);
			}

			if (part.mEndByte == 0) {
				long fileLength = DownloadManager
						.currentLength(iDownloadable.mOutputFile);

				System.out.println("fileLength:" + fileLength);

				part.mStartByte = (int) fileLength == -1 ? 0 : (int) fileLength;
			}
			in = new BufferedInputStream(conn.getInputStream());
			// open the output file and seek to the start location
			raf = new RandomAccessFile(iDownloadable.mOutputFile, "rw");
			raf.seek(part.mStartByte);

			byte data[] = new byte[Downloadable.BUFFER_SIZE];
			int numRead;
			while ((iDownloadable.mState.isLoading())
					&& ((numRead = in.read(data, 0, Downloadable.BUFFER_SIZE)) != -1)) {
				raf.write(data, 0, numRead);
				// increase the startByte for resume later
				part.mStartByte += numRead;
				// increase the downloaded size
				iDownloadable.downloaded(numRead);
			}

			if (iDownloadable.mState.isLoading()) {
				iDownloadable.mIsFinished = true;
			}
		} catch (IOException e) {
			iDownloadable.setState(State.ERROR);
		} finally {
			try {
				if (raf != null)
					raf.close();
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}

		System.out.println("End thread " + mThreadID);

	}
}