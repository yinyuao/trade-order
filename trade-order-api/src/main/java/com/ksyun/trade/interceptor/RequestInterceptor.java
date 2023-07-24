package com.ksyun.trade.interceptor;

import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.ksyun.common.util.TraceUtils.TRACE_ID;

public class RequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 从Header中获取X-KSY-REQUEST-ID，如果不存在则生成一个新的requestId
        String requestId = request.getHeader("X-KSY-REQUEST-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // 将requestId存储到MDC中，方便在日志中使用
        MDC.put(TRACE_ID, requestId);

        return true; // 返回true表示继续执行请求处理链，返回false表示终止请求处理
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 请求处理完毕后，在渲染视图前执行
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        // 整个请求处理完毕后执行，可以进行一些清理操作
        MDC.remove("traceId");
    }
}
