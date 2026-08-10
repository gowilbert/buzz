package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Test Slides")
public class TeleOp_slides extends LinearOpMode {

    private DcMotor slideMotor;
    private DigitalChannel slideLimit1;
    private DigitalChannel slideLimit2;

    // --- CLASS VARIABLES ---
    private ElapsedTime presetTimer = new ElapsedTime();
    private boolean isTimingRun = false;

    // Split times (in seconds)
    private double time0to375 = 0.0;
    private double time375to750 = 0.0;
    private double totalTravelTime = 0.0;

    // Internal checkpoint tracking
    private boolean passed375 = false;
    private double timeAt375 = 0.0;

    // Preset Target Positions (in Encoder Ticks)
    private final int PRESET_BOTTOM = 0;
    private final int PRESET_MID    = 375;
    private final int PRESET_HIGH   = 750; // Max Height Cap

    // Speed & Cushioning
    private final int SLOW_ZONE_TICKS = 0;
    private final double FAST_POWER   = 1.0;
    private final double SLOW_POWER   = 1.0;
    private final double HOLD_POWER   = 0.05;

    // Target position tracking for preset mode
    private int targetPosition = 0;
    private boolean isPresetMode = false;

    @Override
    public void runOpMode() {
        // Hardware Mapping
        slideMotor = hardwareMap.get(DcMotor.class, "slideMotor");
        slideLimit1 = hardwareMap.get(DigitalChannel.class, "slideLimit1");
        slideLimit2 = hardwareMap.get(DigitalChannel.class, "slideLimit2");

        // Motor Setup
        slideMotor.setDirection(DcMotor.Direction.REVERSE); // Adjust if stick direction is reversed
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Set Limit Switches as Inputs
        slideLimit1.setMode(DigitalChannel.Mode.INPUT);
        slideLimit2.setMode(DigitalChannel.Mode.INPUT);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        boolean wasAtBottomLastFrame = true;

        waitForStart();

        while (opModeIsActive()) {
            // Read limit switch states (TouchSensor.isPressed() returns true when activated)
            boolean limit1Triggered = !slideLimit1.getState();
            boolean limit2Triggered = !slideLimit2.getState();

            // REDUNDANCY CHECK: Either switch tripping means the slide has reached the bottom
            boolean isAtBottom = limit1Triggered || limit2Triggered;

            // Auto-zero encoder on bottom contact
            if (isAtBottom && slideMotor.getCurrentPosition() != 0) {
                slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                if (isPresetMode && targetPosition == PRESET_BOTTOM) {
                    isPresetMode = false; // Goal achieved
                }
            }

            int currentPos = slideMotor.getCurrentPosition();
            double manualStick = -gamepad1.right_stick_y; // Stick UP is positive

            // --- 1. HANDLE GAMEPAD PRESET BUTTONS ---
            if (gamepad1.a) {
                targetPosition = PRESET_MID;  // 375 Ticks
                isPresetMode = true;
            } else if (gamepad1.y) {
                targetPosition = PRESET_HIGH; // 750 Ticks
                isPresetMode = true;
            } else if (gamepad1.x) {
                targetPosition = PRESET_BOTTOM; // 0 Ticks
                isPresetMode = true;
            }

            // --- 2. MANUAL JOYSTICK OVERRIDE ---
            if (Math.abs(manualStick) > 0.1) {
                isPresetMode = false; // Disable preset mode immediately when stick is moved
            }

            // --- 3. MOTOR POWER & LOGIC CALCULATION ---
            double appliedPower = 0.0;

            if (isPresetMode) {
                // --- PRESET AUTOMATION MODE ---
                int error = targetPosition - currentPos;

                // original: Math.abs(error) <= 15
                if (Math.abs(error) <= 5 || (targetPosition == PRESET_BOTTOM && isAtBottom)) {
                    // Target reached or bottom switch hit
                    appliedPower = isAtBottom ? 0.0 : HOLD_POWER;
                } else if (error > 0) {
                    // Moving UP to target
                    appliedPower = (error < SLOW_ZONE_TICKS) ? SLOW_POWER : FAST_POWER;
                } else {
                    // Moving DOWN to target
                    appliedPower = (Math.abs(error) < SLOW_ZONE_TICKS) ? -SLOW_POWER : -FAST_POWER;
                }
            } else {
                // --- MANUAL JOYSTICK MODE ---
                if (manualStick > 0) {
                    // Driving UP
                    if (currentPos >= PRESET_HIGH) {
                        appliedPower = 0.0; // Stop at 750 max ticks
                    } else if (currentPos >= (PRESET_HIGH - SLOW_ZONE_TICKS)) {
                        appliedPower = manualStick * SLOW_POWER; // Cushion near top
                    } else {
                        appliedPower = manualStick * FAST_POWER; // Full speed
                    }
                } else if (manualStick < 0) {
                    // Driving DOWN
                    if (isAtBottom) {
                        appliedPower = 0.0; // Stop on limit switches
                    } else if (currentPos <= SLOW_ZONE_TICKS) {
                        appliedPower = manualStick * SLOW_POWER; // Cushion near bottom
                    } else {
                        appliedPower = manualStick * FAST_POWER; // Full speed
                    }
                } else {
                    // Idle state
                    appliedPower = isAtBottom ? 0.0 : HOLD_POWER;
                }
            }
            // Apply power to motor
            slideMotor.setPower(appliedPower);

            // 1. START RUN: Leaving bottom limit switch going UP
            if (wasAtBottomLastFrame && !isAtBottom && slideMotor.getPower() > 0) {
                presetTimer.reset();
                isTimingRun = true;
                passed375 = false;

                time0to375 = 0.0;
                time375to750 = 0.0;
                totalTravelTime = 0.0;
            }

            // 2. CHECKPOINT: Crossing 375 ticks
            if (isTimingRun && !passed375 && currentPos >= 375) {
                timeAt375 = presetTimer.seconds();
                time0to375 = timeAt375; // Split 1: 0 to 375
                passed375 = true;
            }

            // 3. FINISH LINE: Reaching 750 ticks
            if (isTimingRun && currentPos >= 750) {
                double timeAt750 = presetTimer.seconds();
                time375to750 = timeAt750 - timeAt375; // Split 2: 375 to 750
                totalTravelTime = timeAt750;           // Total travel time
                isTimingRun = false;                   // Stop timing run
            }

            // Reset if slides return to bottom mid-run
            if (isAtBottom) {
                isTimingRun = false;
            }

            wasAtBottomLastFrame = isAtBottom;

            // ... (Your motor power logic) ...

            // 4. TELEMETRY DISPLAY
            telemetry.addLine("=== SLIDE PRESET TIMING ===");
            if (isTimingRun) {
                telemetry.addData("Status", "TIMING RUN... (%.2fs)", presetTimer.seconds());
            } else {
                telemetry.addData("Status", "IDLE / COMPLETE");
            }
            telemetry.addData("0 -> 375 Ticks ", "%.3f s", time0to375);
            telemetry.addData("375 -> 750 Ticks", "%.3f s", time375to750);
            telemetry.addData("Total (0 -> 750)", "%.3f s", totalTravelTime);

            // Diagnostics
            telemetry.addData("Mode", isPresetMode ? "PRESET Target: " + targetPosition : "MANUAL");
            telemetry.addData("Encoder Pos", currentPos);
            telemetry.addData("Applied Power", appliedPower);
            telemetry.addData("Bottom Switch Triggered", isAtBottom);
            telemetry.update();
        }
    }
}