package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@TeleOp(name="TurnTable", group="TechTheatre")
public class TechTheatreTurnTable extends OpMode
{
    AnalogInput controlSignal;
    DcMotorEx motor0;
    DcMotorEx motor1;
    DcMotorEx motor2;

    static double TICKS_PER_REV = 1425.1;

    boolean previousSignal = false;
    Gamepad previousGamepad = new Gamepad();

    Double delta = 1.0;

    public enum AdjustableValue
    {
        SetSpeed,
        SignalThreshold,
    }
    public static AdjustableValue getNextAdjustableValue(AdjustableValue current) {
        AdjustableValue[] values = AdjustableValue.values();
        int nextOrdinal = (current.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }
    public static AdjustableValue getPrevAdjustableValue(AdjustableValue current) {
        AdjustableValue[] values = AdjustableValue.values();
        int nextOrdinal = (current.ordinal() - 1 + values.length) % values.length;
        return values[nextOrdinal];
    }
    private final Map<AdjustableValue,Double> adjustableValues = new HashMap<>(Map.ofEntries(
        Map.entry(AdjustableValue.SetSpeed, 1.0),
        Map.entry(AdjustableValue.SignalThreshold, 2.5)));
    AdjustableValue currentlyAdjustingValue = AdjustableValue.SetSpeed;

    @Override
    public void init() {
        String controlSignalHWMapName = "ch0";
        controlSignal = hardwareMap.tryGet(AnalogInput.class, controlSignalHWMapName); // blue wire
        motor0 = hardwareMap.tryGet(DcMotorEx.class, "motor0");
        motor1 = hardwareMap.tryGet(DcMotorEx.class, "motor1");
        motor2 = hardwareMap.tryGet(DcMotorEx.class, "motor2");
        setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
        setMotorZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        setMotorSpeed(0.0);

        previousGamepad.copy(gamepad1);

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

        // User controls
        boolean valueChanged = false;
        if (gamepad1.dpad_up && !previousGamepad.dpad_up) {
            adjustableValues.replace(currentlyAdjustingValue, adjustableValues.get(currentlyAdjustingValue) + delta);
            valueChanged = true;
        }
        if (gamepad1.dpad_down && !previousGamepad.dpad_down) {
            adjustableValues.replace(currentlyAdjustingValue, adjustableValues.get(currentlyAdjustingValue) - delta);
            valueChanged = true;
        }
        if (gamepad1.dpad_left && !previousGamepad.dpad_left) {
            delta /= 10;
        }
        if (gamepad1.dpad_right && !previousGamepad.dpad_right) {
            delta *= 10;
        }
        if (gamepad1.left_bumper && !previousGamepad.left_bumper) {
            //adjustableValueIndex = (adjustableValueIndex - 1 + adjustableValues.length) % adjustableValues.length;
            currentlyAdjustingValue = getNextAdjustableValue(currentlyAdjustingValue);
        }
        if (gamepad1.right_bumper && !previousGamepad.right_bumper) {
            //adjustableValueIndex = (adjustableValueIndex + 1 ) % adjustableValues.length;
            currentlyAdjustingValue = getPrevAdjustableValue(currentlyAdjustingValue);
        }
        boolean forceOn = false;
        if (gamepad1.a && !previousGamepad.a) {
            forceOn = true;
        }
        boolean forceStop = false;
        if (gamepad1.b && !previousGamepad.b) {
            forceStop = true;
        }


        // The signal is active high -- when the voltage is high, the motor should be on
        double currentSignalVoltage = controlSignal.getVoltage();
        boolean currentSignal = currentSignalVoltage > adjustableValues.get(AdjustableValue.SignalThreshold);

        // If there is a change in the signal, or the user forces the motor change, change the behavior
        if (((currentSignal != previousSignal) && currentSignal) || forceOn || (currentSignal && valueChanged)) {
            setMotorSpeed(adjustableValues.get(AdjustableValue.SetSpeed));
        } else if (((currentSignal != previousSignal) && !currentSignal) || forceStop) {
            setMotorSpeed(0);
        }

        // Print help for controls
        telemetry.addData("a", "force on");
        telemetry.addData("b", "force stop");
        telemetry.addData("dpad up", "increase speed by delta");
        telemetry.addData("dpad down", "decrease speed by delta");
        telemetry.addData("dpad left", "decrease delta by factor of 10");
        telemetry.addData("dpad right", "increase delta by factor of 10");
        telemetry.addData("left bumper", "cycle left through adjustable values");
        telemetry.addData("right bumper", "cycle right through adjustable values");
        telemetry.addData("adjustable values",
            //Arrays.stream(adjustableValues).map(v -> v.label).collect(Collectors.joining(", ")));
            Arrays.stream(AdjustableValue.values()).map(Enum::name).collect(Collectors.joining(", ")));
        telemetry.addData("adjusting value", currentlyAdjustingValue);
        telemetry.addData("delta", delta);
        telemetry.addLine("----------");
        // Print telemetry information
        telemetry.addData("signal", currentSignal);
        telemetry.addData("signal voltage", currentSignalVoltage);
        telemetry.addData("signal threshold", adjustableValues.get(AdjustableValue.SignalThreshold));
        telemetry.addData("set speed (rps)", adjustableValues.get(AdjustableValue.SetSpeed));
        telemetry.addData("set speed (tps)", TICKS_PER_REV * adjustableValues.get(AdjustableValue.SetSpeed));
        if (motor0 != null) telemetry.addData("current speed (0)", motor0.getVelocity());
        if (motor1 != null) telemetry.addData("current speed (1)", motor1.getVelocity());
        if (motor2 != null) telemetry.addData("current speed (2)", motor2.getVelocity());

        previousSignal = currentSignal;
        previousGamepad.copy(gamepad1);
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

    private void setMotorMode(DcMotor.RunMode mode) {
        if (motor0 != null) motor0.setMode(mode);
        if (motor1 != null) motor1.setMode(mode);
        if (motor2 != null) motor2.setMode(mode);
    }

    private void setMotorZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        if (motor0 != null) motor0.setZeroPowerBehavior(behavior);
        if (motor1 != null) motor1.setZeroPowerBehavior(behavior);
        if (motor2 != null) motor2.setZeroPowerBehavior(behavior);
    }
}
