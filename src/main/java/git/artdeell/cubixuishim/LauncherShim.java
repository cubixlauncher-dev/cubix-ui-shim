package git.artdeell.cubixuishim;

import static net.kdt.pojavlaunch.MainActivity.INTENT_MINECRAFT_VERSION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.CubixAccount;
import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.TestStorageActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceFragment;
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader;

import java.io.File;

import git.artdeell.cubix.proprietary.ErrorReceiver;
import git.artdeell.cubix.proprietary.Launcher;
import git.artdeell.cubix.proprietary.LauncherInterface;
import git.artdeell.cubix.proprietary.utils.IntentReceiver;

public class LauncherShim implements LauncherInterface {
    public static void init() {
        Launcher.setInterface(new LauncherShim());
    }

    @Override
    public File getVersionDirectory() {
        return new File(Tools.DIR_HOME_VERSION);
    }

    @Override
    public void downloadAndStartGame(Activity activity, String versionName, ErrorReceiver receiver, IntentReceiver intentReceiver) {
        Log.i("Shim", "Starting to wait...");
        ProgressKeeper.waitUntilDone(()->{
            Log.i("Shim", "Wait is over!");
            try {
                JMinecraftVersionList.Version version = Tools.getVersionInfo(versionName);
                new AsyncMinecraftDownloader(activity, version, versionName, new AsyncMinecraftDownloader.DoneListener() {
                    @Override
                    public void onDownloadDone() {
                        ProgressKeeper.waitUntilDone(() -> {
                            Intent i = new Intent(activity, MainActivity.class);
                            i.putExtra(INTENT_MINECRAFT_VERSION, versionName);
                            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intentReceiver.receiveIntent(i);
                        });
                    }

                    @Override
                    public void onDownloadFailed(Throwable throwable) {
                        Log.i("Shim", "Received onDownloadFailed()");
                        receiver.onError(throwable);
                    }
                });
            }catch (Throwable e) {
                receiver.onError(e);
            }
        });
    }

    @Override
    public void interruptDownload() {
        AsyncMinecraftDownloader.interrupt();
    }

    @Override
    public boolean isDownloading() {
        return false;
    }

    @Override
    public void writeCubixAccount(Context context, String username, String token) {
        new CubixAccount(username, token).save(context);
    }

    @Override
    public String[] readCubixAccount(Context context) {
        CubixAccount account = CubixAccount.getAccount(context);
        if(account == null) return null;
        return new String[] {account.username, account.cubixToken};
    }

    @Override
    public Fragment createLauncherPreferenceFragment(boolean which) {
        if(which) return new LauncherPreferenceVideoFragment();
        else return new LauncherPreferenceControlFragment();
    }

    @Override
    public boolean ensureMinimumMemory(Context context, int minimumMemory, boolean dryRun) {
        LauncherPreferences.loadPreferences(context);
        if(LauncherPreferences.PREF_RAM_ALLOCATION < minimumMemory) {
            if(dryRun) return false;
            LauncherPreferences.DEFAULT_PREF.edit().putInt("allocation", minimumMemory).apply();
            LauncherPreferences.PREF_RAM_ALLOCATION = minimumMemory;
        }
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void eraseSettings() {
        LauncherPreferences.DEFAULT_PREF.edit().clear().commit();
    }

    @Override
    public Intent getLauncherStartupIntent(Context context) {
        return new Intent(context, TestStorageActivity.class);
    }

    @Override
    public int getShimBuildCode() {
        return BuildConfig.VERSION_CODE;
    }

    public void loadSettings(Activity context) {
        LauncherPreferences.computeNotchSize(context);
        LauncherPreferences.loadPreferences(context);
    }

    @Override
    public Intent getGameDirIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(DocumentsContract.buildDocumentUri(context.getString(net.kdt.pojavlaunch.R.string.storageProviderAuthorities), Tools.DIR_GAME_HOME), DocumentsContract.Document.MIME_TYPE_DIR);
        return intent;
    }

}
