package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Arrays;
import java.util.stream.Collectors;

@TeleOp(name="TurnTable", group="TechTheatre")
public class TechTheatreTurnTable extends OpMode
{
    AnalogInput controlSignal;
    DcMotorEx motor0;
    DcMotorEx motor1;
    DcMotorEx motor2;

    static double TICKS_PER_REV = 1425.1;
    Double setSpeed = 1.0;
    Double signalThreshold = 2.5; // Volts

    boolean previousSignal = false;
    Gamepad previousGamepad;

    Double delta = 1.0;

    private static class AdjustableValue
    {
        String label;
        Double value;
        public AdjustableValue(String label, Double value) {
            this.label = label;
            this.value = value;
        }
    }
    private final AdjustableValue[] adjustableValues = new AdjustableValue[]{
        new AdjustableValue("Set Speed", setSpeed),
        new AdjustableValue("Signal Threshold", signalThreshold)};
    int adjustableValueIndex = 0;

    @Override
    public void init() {
        String controlSignalHWMapName = "ch0";
        controlSignal = hardwareMap.tryGet(AnalogInput.class, controlSignalHWMapName); // blue wire
        motor0 = hardwareMap.tryGet(DcMotorEx.class, "motor0");
        motor1 = hardwareMap.tryGet(DcMotorEx.class, "motor1");
        motor2 = hardwareMap.tryGet(DcMotorEx.class, "motor2");

        gamepad1.copy(previousGamepad);

        if (controlSignal == null) {
            telemetry.addLine("Error: control signal is missing from hardwareMap!");
            telemetry.addLine(
                    String.format("Expected name %s, available names: %s",
                            controlSignalHWMapName,
                            String.join(", ", hardwareMap.getAllNames(AnalogInput.class))));
        }

        // TODO: Load the saved values from a config file
    }

    @Override
    public void loop() {
        if (controlSignal == null) {
            return;
        }

        // The signal is active high -- when the voltage is high, the motor should be on
        double currentSignalVoltage = controlSignal.getVoltage();
        boolean currentSignal = currentSignalVoltage > signalThreshold;

        // If there is a change in the signal, change the behavior
        if (currentSignal != previousSignal) {
            if (currentSignal) {
                setMotorSpeed(setSpeed);
            } else {
                setMotorSpeed(0);
            }
        }

        // User controls
        if (gamepad1.dpad_up && !previousGamepad.dpad_up) {
            adjustableValues[adjustableValueIndex].value += delta;
        }
        if (gamepad1.dpad_down && !previousGamepad.dpad_down) {
            adjustableValues[adjustableValueIndex].value -= delta;
        }
        if (gamepad1.dpad_left && !previousGamepad.dpad_left) {
            delta /= 10;
        }
        if (gamepad1.dpad_right && !previousGamepad.dpad_right) {
            delta *= 10;
        }
        if (gamepad1.left_bumper && !previousGamepad.left_bumper) {
            adjustableValueIndex = (adjustableValueIndex - 1 + adjustableValues.length) % adjustableValues.length;
        }
        if (gamepad1.right_bumper && !previousGamepad.right_bumper) {
            adjustableValueIndex = (adjustableValueIndex + 1 ) % adjustableValues.length;
        }

        // Print help for controls
        telemetry.addData("dpad up", "increase speed by delta");
        telemetry.addData("dpad down", "decrease speed by delta");
        telemetry.addData("dpad left", "decrease delta by factor of 10");
        telemetry.addData("dpad right", "increase delta by factor of 10");
        telemetry.addData("left bumper", "cycle left through adjustable values");
        telemetry.addData("right bumper", "cycle right through adjustable values");
        telemetry.addData("adjustable values",
            Arrays.stream(adjustableValues).map(v -> v.label).collect(Collectors.joining(", ")));
        telemetry.addData("adjusting value", adjustableValues[adjustableValueIndex].label);
        telemetry.addData("delta", delta);
        telemetry.addLine("----------");
        // Print telemetry information
        telemetry.addData("signal", currentSignal);
        telemetry.addData("signal voltage", currentSignalVoltage);
        telemetry.addData("signal threshold", signalThreshold);
        telemetry.addData("set speed (rps)", setSpeed);
        telemetry.addData("set speed (tps)", TICKS_PER_REV * setSpeed);
        if (motor0 != null) telemetry.addData("current speed (0)", motor0.getVelocity());
        if (motor1 != null) telemetry.addData("current speed (1)", motor1.getVelocity());
        if (motor2 != null) telemetry.addData("current speed (2)", motor2.getVelocity());

        previousSignal = currentSignal;
        gamepad1.copy(previousGamepad);
    }

    @Override
    public void stop() {
        // TODO: Save the current values to a config file
    };

    private void setMotorSpeed(double revolutionsPerSecond) {
        if (motor0 != null) motor0.setVelocity(TICKS_PER_REV * revolutionsPerSecond);
        if (motor1 != null) motor1.setVelocity(TICKS_PER_REV * revolutionsPerSecond);
        if (motor2 != null) motor2.setVelocity(TICKS_PER_REV * revolutionsPerSecond);
    }
}
