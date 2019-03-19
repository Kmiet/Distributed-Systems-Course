package pl.edu.agh.jgroups.hashmap;

import java.util.HashMap;

public class DistributedMap implements SimpleStringMap {

    private HashMap<String, Integer> map;

    public DistributedMap() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return null;
    }

    @Override
    public void put(String key, Integer value) {

    }

    @Override
    public Integer remove(String key) {
        return null;
    }
}
