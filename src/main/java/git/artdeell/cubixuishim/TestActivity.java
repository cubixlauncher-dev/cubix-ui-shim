package git.artdeell.cubixuishim;

import static net.kdt.pojavlaunch.MainActivity.INTENT_MINECRAFT_VERSION;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.JMinecraftVersionList;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.ProgressListener;
import net.kdt.pojavlaunch.tasks.AsyncAssetManager;
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader;
import net.kdt.pojavlaunch.tasks.AsyncVersionList;
import net.kdt.pojavlaunch.value.MinecraftAccount;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.IOException;

public class TestActivity extends AppCompatActivity {
    TextView testView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        testView = findViewById(R.id.test_view);
        LauncherPreferences.DEFAULT_PREF.edit().putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "(Default)").commit();
        ProgressKeeper.addListener(ProgressLayout.DOWNLOAD_MINECRAFT, new ProgressListener() {
            @Override
            public void onProgressStarted() {
                Log.i("TEST", "Progress started");
            }

            @Override
            public void onProgressUpdated(int progress, int resid, Object... va) {
                runOnUiThread(()->{
                    if(resid != -1) testView.setText(progress+"% "+getString(resid, va));
                    else testView.setText(progress+"%");
                });
            }

            @Override
            public void onProgressEnded() {
                Log.i("TEST", "Progress over");
            }
        });
        AsyncAssetManager.unpackComponents(this);
        AsyncAssetManager.unpackSingleFiles(this);
        ProgressKeeper.waitUntilDone(()->{
            Log.i("TEST", "Getting version list...");
            new AsyncVersionList().getVersionList(versions -> {
                ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions);
                JMinecraftVersionList.Version version = Tools.getVersionInfo("hitech");
                new AsyncMinecraftDownloader(this, version, "hitech", new AsyncMinecraftDownloader.DoneListener() {
                    @Override
                    public void onDownloadDone() {
                        ProgressKeeper.waitUntilDone(()->{
                            Intent i = new Intent(TestActivity.this, MainActivity.class);
                            i.putExtra(INTENT_MINECRAFT_VERSION, "hitech");
                            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid()); //You should kill yourself, NOW!
                        });
                    }

                    @Override
                    public void onDownloadFailed(Throwable throwable) {
                        Log.i("TEST", "download failed!");
                        throwable.printStackTrace();
                    }
                });
            });
        });

    }
}