package git.artdeell.cubixuishim;

import android.app.Application;

public class StaticInit extends Application {
    static {
        ProgressShim.init();
    }
}
