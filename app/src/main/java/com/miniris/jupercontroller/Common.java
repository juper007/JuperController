package com.miniris.jupercontroller;

import java.util.UUID;

/**
 * Created by v-mipark on 2/9/2015.
 */
public interface Common {
    public final int REQUEST_BLUETOOTH_ENABLE = 0;
    public final UUID MY_UUID = UUID.fromString("0b3c15dd-063a-4921-9bda-103693a1e26f");
    public final String CONTROLLER_NAME = "Juper Controller";

    public final int MESSAGE_READ = 0;
    public final int MESSAGE_WRITE = 1;

    public final int COMMAND_UNKNOWN = 0;
    public final int COMMAND_IMG = 1;
    public final int COMMAND_STATE = 20;

    public final int MAX_MOTOR_VALUE = 99;
    public final int MIN_MOTOR_VALUE = 0;

    public final int MOTOR_VALUE_OFFSET = 5;
}
