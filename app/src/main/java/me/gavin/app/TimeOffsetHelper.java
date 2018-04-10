package me.gavin.app;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2018/4/10
 */
public final class TimeOffsetHelper {

    public static String getOffsetTime(long timeInMillis) {
        long now = System.currentTimeMillis();
        boolean isBefore = now > timeInMillis;
        String result = isBefore
                ? getTimeOffset(now, timeInMillis)
                : getTimeOffset(timeInMillis, now);
        return isBefore ? result.isEmpty() ? "刚刚" : result + "前"
                : result.isEmpty() ? "马上" : result + "后";
    }

    /**
     * 友好的时间显示
     */
    private static String getTimeOffset(long millisL, long millisS) {

        // 小于一分钟显示 刚刚
        if (millisL - millisS < TimeUnit.MINUTES.toMillis(1)) return "";

        // 小于一小时显示 x分钟前
        if (millisL - millisS < TimeUnit.HOURS.toMillis(1))
            return (millisL - millisS) / TimeUnit.MINUTES.toMillis(1) + " 分钟";

        // 小于一天显示 x小时前
        if (millisL - millisS < TimeUnit.DAYS.toMillis(1))
            return (millisL - millisS) / TimeUnit.HOURS.toMillis(1) + " 小时";

        String dayStr = (millisL - millisS) / TimeUnit.DAYS.toMillis(1) + " 天";

        Calendar calS = Calendar.getInstance();
        calS.setTimeInMillis(millisS);
        Calendar calL = Calendar.getInstance();
        calL.setTimeInMillis(millisL);

        int dd = calL.get(Calendar.DAY_OF_MONTH) - calS.get(Calendar.DAY_OF_MONTH);
        int dm = calL.get(Calendar.MONTH) - calS.get(Calendar.MONTH);
        int dy = calL.get(Calendar.YEAR) - calS.get(Calendar.YEAR);
        //按照减法原理，先day相减，不够向month借；然后month相减，不够向year借；最后year相减。
        if (dd < 0) {
            dm -= 1;
            calL.add(Calendar.MONTH, -1);//得到上一个月，用来得到上个月的天数。
        }
        if (dm < 0) {
            dm = (dm + 12) % 12;
            dy--;
        }

        // 大于十二个月显示 x年前
        if (dy > 0) return dy + " 年";

        // 大于一个月显示 x个月前
        if (dm > 0) return dm + " 个月";

        // 大于一天显示 x天前
        return dayStr;
    }
}
