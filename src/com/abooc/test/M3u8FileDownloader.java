package com.abooc.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import net.chilicat.m3u8.Element;
import net.chilicat.m3u8.Playlist;

import com.luugiathuy.apps.downloadmanager.DownloadManager;
import com.luugiathuy.apps.downloadmanager.Downloadable.Part;

public class M3u8FileDownloader {
	static String m3u8_url = "http://hot.vrs.sohu.com/ipad113781_4613391929782_434813.m3u8?plat=3";
	static String image_url = "http://www.sinaimg.cn/dy/slidenews/69_img/2014_25/56732_53864_449154.jpg";

	public static void main(String[] args) {
		try {
			URL url = new URL(image_url);
			Part part = new Part(url, "56732_53864_449154.jpg", 0, 0);
			DownloadManager.downloadPart(part);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	static void testParseM3u8(String m3u8Url) {
		InputStream in = null;
		try {
			URI uri = URI.create(m3u8Url);
			in = uri.toURL().openStream();
			Playlist playlist = Playlist.parse(in);

			List<Element> list = playlist.getElements();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < list.size(); i++) {
				buffer.append(list.get(i).toString()).append("\n");
			}

			System.out.println(buffer.toString());
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
	}

	static void download(String url, String filename) {
		URL address;
		try {
			address = new URL(url);
			String fileURL = address.getFile();
			String fileName = fileURL.substring(fileURL.lastIndexOf('/') + 1);
			DownloadManager.getInstance().createDownload(
					address, fileName);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
