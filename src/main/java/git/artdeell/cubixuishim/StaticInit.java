package git.artdeell.cubixuishim;

import android.app.Application;

import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.tasks.AsyncVersionList;

public class StaticInit extends Application {
    static {
        ProgressShim.init();
        LauncherShim.init();
        new AsyncVersionList().getVersionList(versions -> {
            ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions);
        });
    }
}
