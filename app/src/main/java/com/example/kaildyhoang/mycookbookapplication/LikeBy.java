package com.example.kaildyhoang.mycookbookapplication;

/**
 * Created by Microsoft Windows on 02/07/2017.
 */

public class LikeBy {
    private String likeById;
    private String likeByName;

    public LikeBy(String likeById, String likeByName) {
        this.likeById = likeById;
        this.likeByName = likeByName;
    }
    public LikeBy() {

    }
    public String getLikeById() {
        return likeById;
    }

    public void setLikeById(String likeById) {
        this.likeById = likeById;
    }

    public String getLikeByName() {
        return likeByName;
    }

    public void setLikeByName(String likeByName) {
        this.likeByName = likeByName;
    }
}
