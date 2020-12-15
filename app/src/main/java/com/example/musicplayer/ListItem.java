package com.example.musicplayer;

/**
 * @author dhx
 * ListView的实体类
 */
public class ListItem {
    String singerText;
    String songText;
    String resourceUrl;
    public ListItem() {
    }

    public ListItem(String singerText, String songText) {
        this.singerText = singerText;
        this.songText = songText;
    }

    public ListItem(String singerText, String songText, String resourceUrl) {
        this.singerText = singerText;
        this.songText = songText;
        this.resourceUrl = resourceUrl;
    }

    public String getSingerText() {
        return singerText;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public void setSingerText(String singerText) {
        this.singerText = singerText;
    }

    public String getSongText() {
        return songText;
    }

    public void setSongText(String songText) {
        this.songText = songText;
    }
}
