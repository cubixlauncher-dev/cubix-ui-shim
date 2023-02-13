package git.artdeell.cubixuishim;

import static net.kdt.pojavlaunch.MainActivity.INTENT_MINECRAFT_VERSION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader;

import java.io.File;

import git.artdeell.cubix.proprietary.ErrorReceiver;
import git.artdeell.cubix.proprietary.Launcher;
import git.artdeell.cubix.proprietary.LauncherInterface;

public class LauncherShim implements LauncherInterface {
    public static void init() {
        Launcher.setInterface(new LauncherShim());
    }

    @Override
    public File getVersionDirectory() {
        return new File(Tools.DIR_HOME_VERSION);
    }

    @Override
    public void downloadAndStartGame(Activity activity, String versionName, ErrorReceiver receiver) {
        ProgressKeeper.waitUntilDone(()->{
            try {
                JMinecraftVersionList.Version version = Tools.getVersionInfo(versionName);
                new AsyncMinecraftDownloader(activity, version, versionName, new AsyncMinecraftDownloader.DoneListener() {
                    @Override
                    public void onDownloadDone() {
                        ProgressKeeper.waitUntilDone(() -> {
                            Intent i = new Intent(activity, MainActivity.class);
                            i.putExtra(INTENT_MINECRAFT_VERSION, versionName);
                            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            activity.startActivity(i);
                            activity.finish();
                            android.os.Process.killProcess(android.os.Process.myPid()); //You should kill yourself, NOW!
                        });
                    }

                    @Override
                    public void onDownloadFailed(Throwable throwable) {
                        receiver.onError(throwable);
                    }
                });
            }catch (Throwable e) {
                receiver.onError(e);
            }
        });
    }

}
