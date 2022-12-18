package ch.heig_vd.daa_labo4

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import java.util.concurrent.TimeUnit

/*
 * Authors: Eliott Chytil, Maxim Golay & Lucien Perregaux
 */
class MainActivity : AppCompatActivity() {
    private val workManager = WorkManager.getInstance(applicationContext)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val adapter = RecyclerAdapter(lifecycleScope, cacheDir)
        recycler.adapter = adapter
        recycler.layoutManager = GridLayoutManager(this, 3)

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresDeviceIdle(true)
            .build()

        val myPeriodicWorkRequest = PeriodicWorkRequestBuilder<CacheWork>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork("I like trains", ExistingPeriodicWorkPolicy.KEEP, myPeriodicWorkRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_clear_cache -> {
                val myWorkRequest = OneTimeWorkRequestBuilder<CacheWork>().build()
                workManager.enqueue(myWorkRequest)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class CacheWork(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
        override fun doWork(): Result {
            cacheDir.listFiles()?.iterator()?.forEach {
                it.delete()
            }


            return Result.success()
        }
    }
}
