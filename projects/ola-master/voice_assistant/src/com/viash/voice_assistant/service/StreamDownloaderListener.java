package com.viash.voice_assistant.service;

import java.io.File;

public interface StreamDownloaderListener {
	public void statusUpdate(int status);
	public void downloadUpdate(int totalKbRead, File downloadFile, boolean force);
	public void downloadSuccess(File downloadFile);
	public void setDownloadTotalSize(int totalSize);//设置下载文件的大小
}
