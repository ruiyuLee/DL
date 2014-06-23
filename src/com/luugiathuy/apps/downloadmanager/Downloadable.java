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

package com.luugiathuy.apps.downloadmanager;

import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;

public abstract class Downloadable extends Observable implements Runnable {

	protected Part mPart;

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

	/** Contants for download's state */
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

	/** The state of the download */
	protected State mState;

	/**
	 * Constructor
	 * 
	 * @param fileURL
	 * @param outputFolder
	 * @param numConnections
	 */
	protected Downloadable(Part part, int numConnections) {
		mPart = part;
		System.out.println("File name: " + mPart.mOutputFile);
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

	public static class Part {
		public URL mURL;
		public String mOutputFile;
		public int mStartByte;
		public int mEndByte;
		public boolean mIsFinished;
		/** Size of the downloaded file (in bytes) */
		protected int mFileSize = -1;

		public Part(URL url, String outputFile, int startByte, int endByte) {
			mURL = url;
			mOutputFile = outputFile;
			mStartByte = startByte;
			mEndByte = endByte;
			mIsFinished = false;
		}

	}

	/**
	 * Thread to download part of a file
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
}
