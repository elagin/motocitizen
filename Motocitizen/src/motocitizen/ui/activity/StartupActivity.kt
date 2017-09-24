package motocitizen.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import motocitizen.datasources.preferences.Preferences
import motocitizen.datasources.preferences.Preferences.Stored.LOGIN
import motocitizen.datasources.preferences.Preferences.Stored.PASSWORD
import motocitizen.main.R
import motocitizen.permissions.Permissions
import motocitizen.router.Router
import motocitizen.user.Auth
import motocitizen.user.User

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
            return
        }
        setContentView(R.layout.activity_startup)
    }

    //TODO проверка разрешений
    public override fun onResume() {
        super.onResume()
        Permissions.requestLocation(this, this::ahead)
    }


    private fun ahead() {
        if (Preferences.anonymous) {
            Router.goTo(this, Router.Target.MAIN)
            return
        }
        if (Preferences.login == "") {
            Router.goTo(this, Router.Target.AUTH)
            return
        }
        Auth.auth(
                LOGIN.string(),
                PASSWORD.string(),
                { Router.goTo(this@StartupActivity, if (User.isAuthorized) Router.Target.MAIN else Router.Target.AUTH) })
    }
}
