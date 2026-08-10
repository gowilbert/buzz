package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DigitalChannel;

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

    private DigitalChannel limitSwitch;
    private DigitalChannel led01_Red;
    private DigitalChannel led02_Green;

    @Override
    public void onInit() {
        limitSwitch = hardwareMap.get(DigitalChannel.class, "limitSwitch");
        limitSwitch.setMode(DigitalChannel.Mode.INPUT);
        led01_Red = hardwareMap.get(DigitalChannel.class, "led01");
        led02_Green = hardwareMap.get(DigitalChannel.class, "led02");

        // Set direction to OUTPUT so the Control Hub sends voltage OUT
        led01_Red.setMode(DigitalChannel.Mode.OUTPUT);
        led02_Green.setMode(DigitalChannel.Mode.OUTPUT);

        // Initial state: Off
        led01_Red.setState(false);
        led02_Green.setState(false);
    }
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
    @Override
    public void onUpdate() {
        if (gamepad1.a) {
            led01_Red.setState(true);  // ON
        } else if (gamepad1.b) {
            led02_Green.setState(true);   // ON
        } else {
            led01_Red.setState(false); // OFF
            led02_Green.setState(false);    // OFF
        }

        if (!limitSwitch.getState()) {
            telemetry.addData("Limit Switch", "PRESSED");
        } else {
            telemetry.addData("Limit Switch", "NOT PRESSED");
        }

        telemetry.update();
    }
}
