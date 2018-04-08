package me.gavin.util.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Gson 排除策略 - 序列化字段过滤
 *
 * @author gavin.xiong 2018/3/8
 */
public final class SerializationExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        JsonIgnore ignore = f.getAnnotation(JsonIgnore.class);
        return ignore != null && !ignore.serialize();
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
