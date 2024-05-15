package ai.utiliti.bes.interceptors;

import com.google.common.util.concurrent.RateLimiter;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public class RateLimitInterceptor implements Interceptor {
    private final RateLimiter rateLimiter = RateLimiter.create(2);

    @Override
    public Response intercept(Chain chain) throws IOException {
        rateLimiter.acquire(1);
        return chain.proceed(chain.request());
    }
}