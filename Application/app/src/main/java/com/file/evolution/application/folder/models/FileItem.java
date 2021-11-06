package com.file.evolution.application.folder.models;

public class FileItem {
    private String mFileName;
    private String mFilePath;
    private String mFileThumbnail;
    private String mFileSize;
    private String mFileModified;
    
    public void setFileName(String mFileName)
    {
        this.mFileName = mFileName;
    }
    
    public String getFileName(){
        return mFileName;
    }
    
    public void setFilePath(String mFilePath)
    {
        this.mFilePath = mFilePath;
    }

    public String getFilePath(){
        return mFilePath;
    }
    
    public void setFileThumbnail(String mFileThumbnail)
    {
        this.mFileThumbnail = mFileThumbnail;
    }

    public String getFileThumbnail(){
        return mFileThumbnail;
    }
    
    public void setFileSize(String mFileSize)
    {
        this.mFileSize = mFileSize;
    }

    public String getFileSize(){
        return mFileSize;
    }
    
    public void setFileModified(String mFileModified)
    {
        this.mFileModified = mFileModified;
    }

    public String getFileModified(){
        return mFileModified;
    }
}
