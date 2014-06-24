package sdk.android.downloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;

/**
 * 
 * @author dayu E-Mail:allnet@live.cn
 * @date 2014年6月24日
 *
 */
public abstract class Downloadable extends Observable implements Runnable {

	protected Part mPart;
	protected String mOutputFile;

	/** Number of connections (threads) to download the file */
	protected int mNumConnections;

	/** downloaded size of the file (in bytes) */
	protected int mDownloaded;

	/** List of download threads */
	protected ArrayList<DownloadThread> mListDownloadThread;

	// Contants for block and buffer size
	protected static final int BLOCK_SIZE = 4096;
	protected static final int BUFFER_SIZE = 4096;
	protected static final int MIN_DOWNLOAD_SIZE = BLOCK_SIZE * 100;
	/** The state of the download */
	protected State mState;

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
	 * Constructor
	 * 
	 * @param fileURL
	 * @param outputFolder
	 * @param numConnections
	 */
	protected Downloadable(Part part, int numConnections, String outputFile) {
		mPart = part;
		mOutputFile = outputFile;
		System.out.println("File name: " + mOutputFile);
		mNumConnections = numConnections;
		mState = State.DOWNLOADING;
		mDownloaded = 0;

		mListDownloadThread = new ArrayList<DownloadThread>();
	}

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
	 * Cancel the downloader
	 */
	public void cancel() {
		setState(State.CANCELLED);
	}

	/**
	 * Get the URL (in String)
	 */
	public String getURL() {
		return mPart.mURL.getFile();
	}

	/**
	 * Get the downloaded file's size
	 */
	public int getFileSize() {
		return mPart.mFileSize;
	}

	/**
	 * Get the current progress of the download
	 */
	public float getProgress() {
		return ((float) mDownloaded / mPart.mFileSize) * 100;
	}

	/**
	 * Get current state of the downloader
	 */
	public State getState() {
		return mState;
	}

	/**
	 * Set the state of the downloader
	 */
	protected void setState(State state) {
		mState = state;
		stateChanged();
	}

	/**
	 * Start or resume download
	 */
	protected void download() {
		new Thread(this).start();
	}

	/**
	 * Increase the downloaded size
	 */
	protected synchronized void downloaded(int value) {
		mDownloaded += value;
		stateChanged();
	}

	/**
	 * Set the state has changed and notify the observers
	 */
	protected void stateChanged() {
		setChanged();
		notifyObservers();
	}

	/**
	 * 负责下载数据的线程
	 */
	protected abstract class DownloadThread implements Runnable {
		protected int mThreadID;
		protected Thread mThread;
		protected Part iPart;

		public DownloadThread(int threadID, Part part) {
			mThreadID = threadID;
			iPart = part;
			download();
		}

		/**
		 * Get whether the thread is finished download the part of file
		 */
		public boolean isFinished() {
			return iPart.mIsFinished;
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

	}

	/**
	 * 下载片段
	 * 
	 * @author dayu E-Mail:allnet@live.cn
	 * @date 2014年6月24日
	 * 
	 */
	public static class Part {
		public URL mURL;
		public int mStartByte;
		public int mEndByte;
		public boolean mIsFinished;
		/** Size of the downloaded file (in bytes) */
		protected int mFileSize = -1;

		/**
		 * 下载片段
		 * 
		 * @param url
		 *            下载地址
		 * @param startByte
		 *            开始下载位置
		 * @param endByte
		 *            终点下载位置，＝0时，下载终点为文件结尾
		 */
		public Part(URL url, int startByte, int endByte) {
			mURL = url;
			mStartByte = startByte;
			mEndByte = endByte;
			mIsFinished = false;
		}

	}

}
