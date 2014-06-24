package com.abooc.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import sdk.android.downloader.DownloadManager;
import sdk.android.downloader.Downloadable.Part;

public class M3u8FileDownloader extends Observable {
	static String m3u8_url = "http://hot.vrs.sohu.com/ipad113781_4613391929782_434813.m3u8?plat=3";
	static String image_url = "http://www.sinaimg.cn/dy/slidenews/69_img/2014_25/56732_53864_449154.jpg";

	public static void main(String[] args) {
		testDownloadImage();
	}

	static void testDownloadImage() {
		try {
			String outputFile = "56732_53864_449154.jpg";
			URL url = new URL(image_url);
			Part part = new Part(url, 0, 256287 / 2);// 下载前一半
			Part part2 = new Part(url, 0, 256287);// 下载后一半
			List<Part> list = new ArrayList<Part>(2);
			list.add(part);
			list.add(part2);
			DownloadManager.downloadPart(list, outputFile);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	//
	// static List<Part> testToPart(List<Element> list) {
	// int i = 0;
	// int length = 2;
	// List<Part> listPort = new ArrayList<Part>();
	// try {
	// for (; i < length; i++) {
	// Element e = list.get(i);
	// Part part = new Part(e.getURI().toURL(), 0, 0);
	// listPort.add(part);
	// }
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// }
	// return listPort;
	// }
	//
	// static List<Element> testParseM3u8(String m3u8Url) {
	// InputStream in = null;
	// try {
	// URI uri = URI.create(m3u8Url);
	// in = uri.toURL().openStream();
	// Playlist playlist = Playlist.parse(in);
	//
	// List<Element> list = playlist.getElements();
	// // StringBuffer buffer = new StringBuffer();
	// // for (int i = 0; i < list.size(); i++) {
	// // buffer.append(list.get(i).toString()).append("\n");
	// // }
	// //
	// // System.out.println(buffer.toString());
	// return list;
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (in != null) {
	// try {
	// in.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// return null;
	// }
	//
	// static void download(String url, String filename) {
	// URL address;
	// try {
	// address = new URL(url);
	// String fileURL = address.getFile();
	// String fileName = fileURL.substring(fileURL.lastIndexOf('/') + 1);
	// DownloadManager.getInstance().createDownload(address, fileName);
	//
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// }
	// }

}
