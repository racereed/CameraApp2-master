package camera1.themaestrochef.com.cameraappfordogs.Models;

public class AnimalsModel {
    int imageRes,audioRes;

    public AnimalsModel(int imageRes, int audioRes) {
        this.imageRes = imageRes;
        this.audioRes = audioRes;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public int getAudioRes() {
        return audioRes;
    }

    public void setAudioRes(int audioRes) {
        this.audioRes = audioRes;
    }
}
