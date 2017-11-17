package si.fri.rsobook.model;

public class RoomStats {

    public String name;

    public Integer userCount;

    public RoomStats(String name) {
        this.name = name;
        this.userCount = 0;
    }
}
