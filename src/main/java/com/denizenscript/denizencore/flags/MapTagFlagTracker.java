package com.denizenscript.denizencore.flags;

import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MapTagFlagTracker extends AbstractFlagTracker {

    public MapTag map;

    public MapTagFlagTracker(MapTag map) {
        this.map = map;
    }

    public static StringHolder valueString = new StringHolder("__value");

    public static StringHolder expirationString = new StringHolder("__expiration");

    public boolean needsClean = false;

    public static boolean isExpired(ObjectTag expirationObj) {
        if (expirationObj == null) {
            return false;
        }
        if (System.currentTimeMillis() > ((DurationTag) expirationObj).getMillis()) {
            return true;
        }
        return false;
    }

    @Override
    public ObjectTag getFlagValue(String key) {
        List<String> splitKey = CoreUtilities.split(key, '.');
        String endKey = splitKey.get(splitKey.size() - 1);
        MapTag map = this.map;
        for (int i = 0; i < splitKey.size() - 1; i++) {
            ObjectTag subMap = map.map.get(new StringHolder(splitKey.get(i)));
            if (!(subMap instanceof MapTag) || !((MapTag) subMap).isFlagMap) {
                return null;
            }
            map = (MapTag) subMap;
        }
        ObjectTag obj = map.map.get(new StringHolder(endKey));
        if (obj instanceof MapTag) {
            ObjectTag value = ((MapTag) obj).map.get(valueString);
            if (value == null) {
                return null;
            }
            if (isExpired(((MapTag) obj).map.get(expirationString))) {
                needsClean = true;
                return null;
            }
            return value;
        }
        return null;
    }

    @Override
    public DurationTag getFlagExpirationTime(String key) {
        return null;
    }

    @Override
    public Collection<String> listAllFlags() {
        ArrayList<String> keys = new ArrayList<>(map.map.size());
        for (StringHolder string : map.map.keySet()) {
            keys.add(string.str);
        }
        return keys;
    }

    public void doClean(MapTag map) {
        ArrayList<StringHolder> toRemove = new ArrayList<>();
        for (Map.Entry<StringHolder, ObjectTag> entry : map.map.entrySet()) {
            if (entry.getKey().equals(valueString) || entry.getKey().equals(expirationString)) {
                continue;
            }
            if (entry.getValue() instanceof MapTag && ((MapTag) entry.getValue()).isFlagMap) {
                if (isExpired(((MapTag) entry.getValue()).map.get(expirationString))) {
                    toRemove.add(entry.getKey());
                }
                else {
                    doClean(map);
                }
            }
        }
        for (StringHolder str : toRemove) {
            map.map.remove(str);
        }
    }

    @Override
    public void setFlag(String key, ObjectTag value, DurationTag expiration) {
        if (needsClean) {
            doClean(map);
        }
        List<String> splitKey = CoreUtilities.split(key, '.');
        String endKey = splitKey.get(splitKey.size() - 1);
        for (int i = 0; i < splitKey.size() - 1; i++) {
            ObjectTag subMap = map.map.get(new StringHolder(splitKey.get(i)));
            if (!(subMap instanceof MapTag) || !((MapTag) subMap).isFlagMap) {
                subMap = new MapTag();
                ((MapTag) subMap).isFlagMap = true;
                map.map.put(new StringHolder(splitKey.get(i)), subMap);
            }
            map = (MapTag) subMap;
        }
        MapTag resultMap = new MapTag();
        resultMap.isFlagMap = true;
        resultMap.map.put(valueString, value);
        resultMap.map.put(expirationString, expiration);
        map.map.put(new StringHolder(endKey), resultMap);
    }
}
