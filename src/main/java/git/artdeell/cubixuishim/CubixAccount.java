package git.artdeell.cubixuishim;

import android.content.Context;

public class CubixAccount {
    private final String token;
    private final String username;

    public CubixAccount(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public static CubixAccount get(Context context) {
        net.kdt.pojavlaunch.CubixAccount openAccount = net.kdt.pojavlaunch.CubixAccount.getAccount(context);
        if(openAccount == null) return null;
        return new CubixAccount(openAccount.cubixToken, openAccount.username);
    }

    public void save(Context context) {
        toOpenAccount().save(context);
    }

    private net.kdt.pojavlaunch.CubixAccount toOpenAccount() {
        return new net.kdt.pojavlaunch.CubixAccount(token, username);
    }
}
