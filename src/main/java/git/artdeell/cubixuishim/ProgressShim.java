package git.artdeell.cubixuishim;

import android.content.Context;
import android.util.Log;

import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener;
import net.kdt.pojavlaunch.services.ProgressServiceKeeper;

import java.util.HashMap;

import git.artdeell.cubix.proprietary.ProgressCountListener;
import git.artdeell.cubix.proprietary.ProgressGranter;
import git.artdeell.cubix.proprietary.ProgressInfo;
import git.artdeell.cubix.proprietary.ProgressListener;

public class ProgressShim implements ProgressGranter {

    private static final HashMap<ProgressListener, ListenerWrapper> wrappers = new HashMap<>();
    private static final HashMap<ProgressCountListener, CountWrapper> countWrappers = new HashMap<>();

    public static void init() {
        ProgressInfo.setGranter(new ProgressShim());
    }

    @Override
    public void addListener(String label, ProgressListener listener) {
        ListenerWrapper wrapper = new ListenerWrapper(listener);
        wrappers.put(listener, wrapper);
        ProgressKeeper.addListener(label, wrapper);
    }

    @Override
    public void removeListener(String label, ProgressListener listener) {
        ListenerWrapper wrapper = wrappers.get(listener);
        if(wrapper != null) {
            ProgressKeeper.removeListener(label, wrapper);
            wrappers.remove(listener);
        }
    }

    @Override
    public void addCountListener(ProgressCountListener listener) {
        CountWrapper wrapper = new CountWrapper(listener);
        countWrappers.put(listener, wrapper);
        ProgressKeeper.addTaskCountListener(wrapper);
    }

    @Override
    public void removeCountListener(ProgressCountListener listener) {
        CountWrapper wrapper = countWrappers.get(listener);
        if(wrapper != null) {
            ProgressKeeper.removeTaskCountListener(wrapper);
            countWrappers.remove(listener);
        }
    }

    @Override
    public void performWhenOver(Runnable runnable) {
        ProgressKeeper.waitUntilDone(runnable);
    }

    @Override
    public int getTaskCount() {
        return ProgressKeeper.getTaskCount();
    }

    @Override
    public Object installService(Context context) {
        ProgressServiceKeeper keeper = new ProgressServiceKeeper(context);
        ProgressKeeper.addTaskCountListener(keeper);
        return keeper;
    }

    @Override
    public void uninstallService(Object object) {
        ProgressKeeper.removeTaskCountListener((TaskCountListener) object);
    }

    @Override
    public void postProgress(String label, int prog, int resid, Object[] va) {
        ProgressKeeper.submitProgress(label, prog, resid, va);
    }

    static class ListenerWrapper implements net.kdt.pojavlaunch.progresskeeper.ProgressListener {
        private final ProgressListener listener;
        public ListenerWrapper(ProgressListener listener) {
            this.listener = listener;
        }
        @Override
        public void onProgressStarted() {
            listener.progressStarted();
        }

        @Override
        public void onProgressUpdated(int progress, int resid, Object... va) {
            listener.progressUpdated(progress, resid, va);
        }

        @Override
        public void onProgressEnded() {
            Log.i("ProgressSkim", "progressEnded");
            listener.progressEnded();
        }
    }
    static class CountWrapper implements TaskCountListener {
        private final ProgressCountListener listener;
        public CountWrapper(ProgressCountListener listener) {
            this.listener = listener;
        }
        @Override
        public void onUpdateTaskCount(int taskCount) {
            listener.updateCount(taskCount);
        }
    }
}
