package pl.edu.agh.jgroups.hashmap;

import java.util.HashMap;
import java.util.Set;

public class DistributedMap implements SimpleStringMap {

    private HashMap<String, Integer> map;

    public DistributedMap() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public HashMap<String, Integer> snapshot() {
        return map;
    }

    public Set<String> getKeySet() {
        return map.keySet();
    }

    @Override
    public Integer get(String key) {
        return map.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        try {
            map.put(key, value);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer remove(String key) {
        Integer result = null;
        try {
            result = map.remove(key);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
