package motocitizen.content;

public class Region {
    public String id;
    public String name;
    public double lon;
    public double lat;

    public String getTitle() {
        return name + " " + id;
    }
}