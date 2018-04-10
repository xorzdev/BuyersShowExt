package me.gavin.app;

import java.util.List;

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2018/4/8
 */
public class ModelResult {

    public List<Task> data;
    public Pages pages;
    public int count;

    public static class Pages {
        public int total;
        public int now;
    }

    @Override
    public String toString() {
        return "ModelResult{" +
                "data=" + data +
                ", pages=" + pages +
                ", count=" + count +
                '}';
    }
}
