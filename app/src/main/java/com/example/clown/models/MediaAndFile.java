package com.example.clown.models;

import android.media.Image;

public class MediaAndFile {
    public String vidPath,imgPath,filePath,finame,timestamp;
    public Image image =null;

    public MediaAndFile(String vidPath,String imgPath,String filePath,String finame,String timestamp ){
        this.vidPath=vidPath;
        this.imgPath=imgPath;
        this.filePath=filePath;
        this.finame=finame;
        this.image=null;
        this.timestamp=timestamp;
    }

    public String getVidPath(){
        return this.vidPath;
    }
    public  String getImgPath(){
        return this.imgPath;
    }
    public  String getFilePath(){
        return this.filePath;
    }
    public String getFiname(){
        return this.finame;
    }

    public void setVidPath(String string){
        this.vidPath=string;
    }

    public void setImgPath(String string){
        this.imgPath=string;
    }

    public void setFiname(String string){
        this.finame=string;
    }
}
