package com.example.weizhi.oddleassignment.background;

import android.app.Application;
import android.util.Log;

import com.octo.android.robospice.SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.springandroid.json.gson.GsonObjectPersisterFactory;

import org.springframework.web.client.RestTemplate;

import roboguice.util.temp.Ln;

/**
 * Subclass Spice Service so that we can use GsonObjectPersistor for our cache.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class MySpiceService extends SpringAndroidSpiceService {
    public MySpiceService(){
        Ln.getConfig().setLoggingLevel(Log.ASSERT);
    }

    @Override
    public RestTemplate createRestTemplate() {
        return null;
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        GsonObjectPersisterFactory gsonObjectPersisterFactory = new GsonObjectPersisterFactory(application);
        cacheManager.addPersister(gsonObjectPersisterFactory);
        return cacheManager;
    }
}
