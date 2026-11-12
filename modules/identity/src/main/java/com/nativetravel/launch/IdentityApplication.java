package com.nativetravel.launch;

import com.nativetravel.distributed.ServiceRuntime;

public final class IdentityApplication {
    public static void main(String[] args) {
        ServiceRuntime.start("identity", 8081, args);
    }
}
