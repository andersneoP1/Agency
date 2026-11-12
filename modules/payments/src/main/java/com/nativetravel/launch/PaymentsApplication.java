package com.nativetravel.launch;

import com.nativetravel.distributed.ServiceRuntime;

public final class PaymentsApplication {
    public static void main(String[] args) {
        ServiceRuntime.start("payments", 8084, args);
    }
}
