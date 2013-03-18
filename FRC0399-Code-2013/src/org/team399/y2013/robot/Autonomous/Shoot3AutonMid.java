/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.team399.y2013.robot.Autonomous;

import edu.wpi.first.wpilibj.Timer;
import org.team399.y2013.robot.Constants;
import org.team399.y2013.robot.Main;

/**
 *
 * @author Jackie
 */
public class Shoot3AutonMid {

    static AutonomousTimerThread timer = new AutonomousTimerThread();
    private static long elapsedTime = 0, start = 0;

    public static void start() {
        start = System.currentTimeMillis();
        //timer.start();
        Main.shooter.start();
        Main.shooter.setShooterSpeed(7900);
        Main.arm.setPointRotations(Constants.ARM_MID_SHOT);
        Main.arm.setEnabled(true);
        System.out.println("Init'd auton");
        finished = false;
    }
    static boolean finished = false;

    public static void run() {
        System.out.println("Running auton, Timer: " + timer.get());
        elapsedTime = System.currentTimeMillis() - start;

        if (!finished) {
            Main.arm.setPointRotations(Constants.ARM_MID_SHOT + .1); //add .1 to make it mid
            Timer.delay(1.85);
            
            Main.feeder.setBelt(.5);
            Timer.delay(.25);
            
            Main.feeder.setKicker(Constants.KICKER_OUT);
            Main.feeder.setBelt(0);
            Timer.delay(.75);
            
            Main.feeder.setKicker(Constants.KICKER_IN);
            Timer.delay(.25);
            
            Main.feeder.setBelt(1.0);
            Timer.delay(1.25);
            
            Main.feeder.setKicker(Constants.KICKER_OUT);
            Main.feeder.setBelt(0);
            Timer.delay(.75);
            
            Main.feeder.setKicker(Constants.KICKER_IN);
            Timer.delay(.25);
            
            Main.feeder.setBelt(1.0);
            Timer.delay(1.25);
            
            Main.feeder.setKicker(Constants.KICKER_OUT);
            Main.feeder.setBelt(0);
            Timer.delay(.75);
            
            Main.feeder.setKicker(Constants.KICKER_IN);
            Timer.delay(.25);
            
            Main.feeder.setBelt(1.0);
            Timer.delay(1.25);
            
            Main.arm.setPointRotations(Constants.ARM_STOW_UP);
            Timer.delay(1.25);
            
            finished = true;
        }

        Main.drive.tankDrive(0, 0);
        Main.feeder.setBelt(0);
        Main.shooter.setShooterSpeed(0);
    }
}