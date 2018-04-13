package me.gavin.app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import me.gavin.base.RequestCode;
import me.gavin.ext.mjx.R;

/**
 * 通知助手
 *
 * @author gavin.xiong 2018/3/6
 */
public final class NotificationHelper {

    public static final String CHANNEL_RUNNING = "running";
    public static final String CHANNEL_TASK = "task";

    public static Notification.Builder newNotificationBuilder(Context context, String channelId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return new Notification.Builder(context);
        } else {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                createChannel(context, channelId);
            }
            return new Notification.Builder(context, channelId);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void createChannel(Context context, String channelId) {
        NotificationChannel channel = null;
        if (CHANNEL_RUNNING.equals(channelId)) {
            channel = new NotificationChannel(CHANNEL_RUNNING, "运行中", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.enableVibration(false);
        } else if (CHANNEL_TASK.equals(channelId)) {
            channel = new NotificationChannel(CHANNEL_TASK, "任务提示", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.enableVibration(false);
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && channel != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static void notify(Context cx, int count, long time) {
        NotificationManagerCompat.from(cx)
                .notify(0x250, newNotificationBuilder(cx, CHANNEL_RUNNING)
                        .setSmallIcon(R.drawable.vt_circle_default_24dp)
                        .setContentTitle(String.format("\"%s\"正在运行", cx.getString(R.string.app_name)))
                        .setContentText(String.format("%s 个任务等待执行，最近的在 %s。", count, TimeOffsetHelper.getOffsetTime(time)))
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setShowWhen(true)
                        .setContentIntent(PendingIntent.getActivity(cx, RequestCode.DONT_CARE,
                                new Intent(cx, MainActivity.class)
                                        .addCategory(Intent.CATEGORY_LAUNCHER)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED), // 关键的一步，设置启动模式
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .setDefaults(NotificationCompat.FLAG_FOREGROUND_SERVICE)
                        .build());
    }

    public static void notify(Context cx, Task task, String msg) {
        NotificationManagerCompat.from(cx)
                .notify(String.valueOf(task.getId()), task.get_id().intValue(), newNotificationBuilder(cx, CHANNEL_TASK)
                        .setSmallIcon(R.drawable.vt_circle_default_24dp)
                        .setContentTitle(task.getName())
                        .setContentText(String.format("已执行 %s 次：%s", task.getCount(), msg))
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setShowWhen(true)
                        .setContentIntent(PendingIntent.getActivity(cx, RequestCode.DONT_CARE,
                                new Intent(cx, MainActivity.class)
                                        .addCategory(Intent.CATEGORY_LAUNCHER)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED), // 关键的一步，设置启动模式
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .setDefaults(NotificationCompat.FLAG_FOREGROUND_SERVICE)
                        .build());
    }
}
