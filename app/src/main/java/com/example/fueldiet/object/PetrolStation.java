package com.example.fueldiet.object;

public class PetrolStation {
    private String name;
    private boolean customImage;
    private String imageName;

    public String getName() {
        return name;
    }

    public boolean isCustomImage() {
        return customImage;
    }

    public String getImageName() {
        return imageName;
    }

    public PetrolStation(String name) {
        this.name = name;
        customImage = false;
        imageName = null;
    }

    public void setImageName(String imageName) {
        this.customImage = true;
        this.imageName = imageName;
    }
}
