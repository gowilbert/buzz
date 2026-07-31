package org.firstinspires.ftc.teamcode;

import dev.nextftc.ftc.Gamepads;
import dev.nextftc.ftc.NextFTCOpMode;
import dev.nextftc.ftc.components.BulkReadComponent;
import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.hardware.driving.DriverControlledCommand;
import dev.nextftc.hardware.driving.MecanumDriverControlled;
import dev.nextftc.hardware.impl.MotorEx;

public abstract class TeleOp_base extends NextFTCOpMode {
    public TeleOp_base() {
        addComponents(
                BulkReadComponent.INSTANCE,
                BindingsComponent.INSTANCE);
    }
    private final MotorEx frontLeftMotor = new MotorEx("leftFront").reversed().brakeMode();
    private final MotorEx frontRightMotor = new MotorEx("rightFront").brakeMode();
    private final MotorEx backLeftMotor = new MotorEx("leftRear").reversed().brakeMode();
    private final MotorEx backRightMotor = new MotorEx("rightRear").brakeMode();
    @Override
    public void onStartButtonPressed() {
        DriverControlledCommand driverControlled = new MecanumDriverControlled(
                frontLeftMotor,
                frontRightMotor,
                backLeftMotor,
                backRightMotor,
                Gamepads.gamepad1().leftStickY().negate(),
                Gamepads.gamepad1().leftStickX(),
                Gamepads.gamepad1().rightStickX());  // Scalar to reduce turn power
        driverControlled.schedule();
    }
}
