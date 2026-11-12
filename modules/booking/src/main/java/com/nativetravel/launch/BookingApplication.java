package com.nativetravel.launch;

import com.nativetravel.distributed.ServiceRuntime;

public final class BookingApplication {
    public static void main(String[] args) {
        ServiceRuntime.start("booking", 8083, args);
    }
}
