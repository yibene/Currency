package cash.practice.currency

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule
import org.kodein.di.android.x.androidXModule
import cash.practice.currency.di.appModule

class Application : Application(), KodeinAware {

    /**
     * For dependency injection
     */
    override val kodein = Kodein.lazy {
        import(androidModule(this@Application))
        import(androidXModule(this@Application))
        import(appModule)
    }

}