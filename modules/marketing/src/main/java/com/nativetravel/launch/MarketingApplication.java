package com.nativetravel.launch;

import com.nativetravel.distributed.ServiceRuntime;

public final class MarketingApplication {
    public static void main(String[] args) {
        ServiceRuntime.start("marketing", 8085, args);
    }
}
