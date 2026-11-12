package com.nativetravel.launch;

import com.nativetravel.distributed.ServiceRuntime;

public final class CatalogApplication {
    public static void main(String[] args) {
        ServiceRuntime.start("catalog", 8082, args);
    }
}
