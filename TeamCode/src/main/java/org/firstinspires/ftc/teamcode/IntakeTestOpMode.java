import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Intake Test OpMode", group = "Testing")
public class IntakeTestOpMode extends LinearOpMode {

    // Declare hardware variables
    private DcMotor intakeMotor;

    // Hardware configuration name (must match your REV Robot Configuration)
    private static final String MOTOR_NAME = "intakeMotor";

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initializing Hardware...");
        telemetry.update();

        // Map the motor from the REV configuration
        intakeMotor = hardwareMap.get(DcMotor.class, MOTOR_NAME);

        // Configure motor behavior
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Switch to REVERSE if positive power pushes game pieces away
        intakeMotor.setDirection(DcMotorSimple.Direction.FORWARD);

        telemetry.addData("Status", "Hardware Ready. Press Play to Test.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Analog trigger control (0.0 to 1.0)
            double intakeIn = gamepad1.right_trigger;   // Pull Right Trigger to Intake
            double intakeOut = gamepad1.left_trigger;  // Pull Left Trigger to Outtake

            // Net power calculation (-1.0 to +1.0)
            double intakePower = intakeIn - intakeOut;

            // Half-speed precision testing button (Hold X / Square)
            if (gamepad1.x) {
                intakePower *= 0.5;
            }

            // Apply power to motor
            intakeMotor.setPower(intakePower);

            // Telemetry output for driver station
            telemetry.addData("--- INTAKE MOTOR STATUS ---", "");
            telemetry.addData("Motor Power Command", "%.2f", intakePower);
            telemetry.addData("Right Trigger (In)", "%.2f", intakeIn);
            telemetry.addData("Left Trigger (Out)", "%.2f", intakeOut);
            telemetry.addData("Half-Speed Mode (X)", gamepad1.x ? "ACTIVE" : "OFF");
            telemetry.update();
        }
    }
}