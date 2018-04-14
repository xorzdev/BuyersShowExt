package me.gavin.app;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
    public static final String CHANNEL_RESULT = "result";

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
            channel = new NotificationChannel(CHANNEL_RUNNING, "运行中", NotificationManager.IMPORTANCE_MIN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.enableVibration(false);
        } else if (CHANNEL_TASK.equals(channelId)) {
            channel = new NotificationChannel(CHANNEL_TASK, "任务提示", NotificationManager.IMPORTANCE_MIN);
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.enableVibration(false);
        } else if (CHANNEL_RESULT.equals(channelId)) {
            channel = new NotificationChannel(CHANNEL_RESULT, "成功", NotificationManager.IMPORTANCE_HIGH);
            channel.setBypassDnd(true);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setShowBadge(true);
            channel.enableVibration(true);
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
                        .setPriority(Notification.PRIORITY_LOW)
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
                        .setPriority(Notification.PRIORITY_LOW)
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

    public static void notify(Context cx, Task task, boolean isSuccess) {
        NotificationManagerCompat.from(cx)
                .notify(String.valueOf(task.getId()), task.get_id().intValue(), newNotificationBuilder(cx, CHANNEL_RESULT)
                        .setSmallIcon(R.drawable.vt_circle_default_24dp)
                        .setContentTitle(task.getName())
                        .setContentText(String.format("已执行 %s 次：%s", task.getCount(), isSuccess ? "成功" : "失败"))
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setShowWhen(true)
                        .setContentIntent(PendingIntent.getActivity(cx, RequestCode.DONT_CARE,
                                new Intent(cx, MainActivity.class)
                                        .addCategory(Intent.CATEGORY_LAUNCHER)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED), // 关键的一步，设置启动模式
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .build());
    }
}
