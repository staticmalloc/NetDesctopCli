package client;

public class CoverageNetData {
    private float startlat;
    private float endlat;
    private float dlong;
    private float dlat;
    private int size;
    private int[] data;
    private String index_type;
    private float startlong;

    public float getStartlat() {
        return startlat;
    }

    public void setStartlat(float startlat) {
        this.startlat = startlat;
    }

    public float getEndlat() {
        return endlat;
    }

    public void setEndlat(float endlat) {
        this.endlat = endlat;
    }

    public float getDlong() {
        return dlong;
    }

    public void setDlong(float dlong) {
        this.dlong = dlong;
    }

    public float getDlat() {
        return dlat;
    }

    public void setDlat(float dlat) {
        this.dlat = dlat;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public String getIndex_type() {
        return index_type;
    }

    public void setIndex_type(String index_type) {
        this.index_type = index_type;
    }

    public float getStartlong() {
        return startlong;
    }

    public void setStartlong(float startlong) {
        this.startlong = startlong;
    }

    public float getEndlong() {
        return endlong;
    }

    public void setEndlong(float endlong) {
        this.endlong = endlong;
    }

    private float endlong;
}
