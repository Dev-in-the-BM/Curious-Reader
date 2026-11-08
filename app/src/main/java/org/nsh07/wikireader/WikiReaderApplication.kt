package org.nsh07.wikireader

import android.app.Application
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import okhttp3.OkHttpClient
import org.nsh07.wikireader.data.AppContainer
import org.nsh07.wikireader.data.DefaultAppContainer
import com.theonionsarewatching.yapchik.Yapchik
import com.theonionsarewatching.yapchik.SoftkeyMode

class WikiReaderApplication : Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        Yapchik.install(this)
        Yapchik.mode = SoftkeyMode.ON
        Yapchik.navigationBarPolicy = Yapchik.NavBarPolicy.HIDE_WITH_BAR
        // This app's Material/Compose content already fills the edge-to-edge
        // window. Do not reserve an extra OEM navigation-strip guard below the
        // softkey bar; it would lift the bar above the system navigation area.
        Yapchik.navGuardDp = 0
        container = DefaultAppContainer(this)
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        OkHttpClient.Builder()
                            .addNetworkInterceptor { chain ->
                                chain.proceed(
                                    chain.request()
                                        .newBuilder()
                                        .header(
                                            "User-Agent",
                                            container.userAgentString
                                        )
                                        .build()
                                )
                            }
                            .build()
                    )
                )
                add(SvgDecoder.Factory(scaleToDensity = true))
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}
