package reversi.graphicengine.models;

public class Slider {
    private boolean collision;
    private float offset, difference;

    public Slider() {
    }

    public boolean isCollision() {
        return collision;
    }

    public void setCollision(boolean collision) {
        this.collision = collision;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public float getDifference() {
        return difference;
    }

    public void setDifference(float difference) {
        this.difference = difference;
    }
}
