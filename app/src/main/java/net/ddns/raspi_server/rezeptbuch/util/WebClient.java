package net.ddns.raspi_server.rezeptbuch.util;

public class WebClient {
    public static void downloadImage(String localPath, String url){

    }

    public static void downloadImage(String localPath, String url, DownloadCallback callback){

    }

    public interface DownloadCallback{
        void finished(boolean success);
    }
}
