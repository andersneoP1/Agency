package com.nativetravel.launch;

import com.nativetravel.distributed.ServiceRuntime;

public final class CommunicationsApplication {
    public static void main(String[] args) {
        ServiceRuntime.start("communications", 8086, args);
    }
}
