package sdk.android.downloader.v1;

import sdk.android.downloader.v1.DownloadManager.Point;

public abstract class DataOuter {
	public String outputFile;

	public DataOuter(String outputFile) {
		this.outputFile = outputFile;
	}

	public abstract void onLoaded(Point point, byte[] data);

}