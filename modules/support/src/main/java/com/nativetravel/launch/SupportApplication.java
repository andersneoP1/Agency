package com.nativetravel.launch;

import com.nativetravel.distributed.ServiceRuntime;

public final class SupportApplication {
    public static void main(String[] args) {
        ServiceRuntime.start("support", 8087, args);
    }
}
