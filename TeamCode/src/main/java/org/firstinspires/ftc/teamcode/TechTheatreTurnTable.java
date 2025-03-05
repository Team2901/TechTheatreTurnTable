package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name="TurnTable", group="TechTheatre")
public class TechTheatreTurnTable extends OpMode
{
    AnalogInput controlSignal;
    DcMotorEx motor0;
    DcMotorEx motor1;
    DcMotorEx motor2;

    static double TICKS_PER_REV = 1425.1;
    double rps = 1;
    double signalThreshold = 2.5; // Volts

    @Override
    public void init() {
        controlSignal = hardwareMap.get(AnalogInput.class, "ch0"); // blue wire
        motor0 = hardwareMap.get(DcMotorEx.class, "motor0");
        motor1 = hardwareMap.get(DcMotorEx.class, "motor1");
        motor2 = hardwareMap.get(DcMotorEx.class, "motor2");
    }

    @Override
    public void loop() {
        // The signal is active high -- when the voltage is high, the motor should be on
        double currentSignalVoltage = controlSignal.getVoltage();
        boolean currentSignal = currentSignalVoltage > signalThreshold;

        // This isn't ideal -- it is constantly calling the API based on the level;
        // really should happen on edges or transitions between levels...
        if (currentSignal) {
            motor0.setVelocity(TICKS_PER_REV * rps);
            motor1.setVelocity(TICKS_PER_REV * rps);
            motor2.setVelocity(TICKS_PER_REV * rps);
        }
        else {
            motor0.setVelocity(0);
            motor1.setVelocity(0);
            motor2.setVelocity(0);
        }

        telemetry.addData("signal", currentSignal);
        telemetry.addData("signal voltage", currentSignalVoltage);
        telemetry.addData("speed (rps)", rps);
        telemetry.addData("speed (tps)", TICKS_PER_REV * rps);
    }
}