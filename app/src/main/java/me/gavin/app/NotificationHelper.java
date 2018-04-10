package me.gavin.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

import me.gavin.base.RequestCode;
import me.gavin.ext.mjx.R;

/**
 * 通知助手
 *
 * @author gavin.xiong 2018/3/6
 */
public final class NotificationHelper {

    public static void notify(Context context, String title, String content, String ticker, String avatar, PendingIntent intent) {
        NotificationManagerCompat.from(context)
                .notify(0x250, buildNotification(context, title, content, ticker, avatar, intent));
    }

    public static void cancel(Context context) {
        NotificationManagerCompat.from(context).cancel(0x250);
    }

    private static Notification buildNotification(Context cx, String tt, String cn, String tk, String av, PendingIntent i) {
        Notification.Builder builder = new Notification.Builder(cx)
                .setSmallIcon(R.drawable.vt_arrow_back_black_24dp)
                // .setLargeIcon(getBitmap(cx, av))
                .setContentTitle(tt)
                .setContentText(cn)
                .setTicker(tk)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentIntent(i)
                // .setActions()
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return builder.build();
    }

    public static Notification buildNotification(Context cx) {
        return new Notification.Builder(cx)
                .setSmallIcon(R.drawable.vt_arrow_back_black_24dp)
                .setContentTitle(String.format("\"%s\"正在运行", cx.getString(R.string.app_name)))
                .setContentText("点击即可了解详情或停止应用")
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setContentIntent(PendingIntent.getActivity(cx, RequestCode.DONT_CARE,
                        new Intent(cx, MainActivity.class)
                                .addCategory(Intent.CATEGORY_LAUNCHER)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED), // 关键的一步，设置启动模式
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setDefaults(NotificationCompat.FLAG_FOREGROUND_SERVICE)
                .build();
    }

    /**
     * 获取网络图片
     *
     * @param url 图片网络地址
     * @return Bitmap 返回位图
     */
    private static Bitmap getBitmap(Context context, String url) {
        try {
            return Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .into(60, 60)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }
    }
}
