/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package team163.commander;

import battlecode.common.MapLocation;
import team163.utils.Move;

/**
 *
 * @author sweetness
 */
public class B_BeastMode implements Behavior {

    double health = Commander.rc.getHealth();
    double curHealth = health;
    boolean wasHurt = false;

    public void perception() {
        curHealth = Commander.rc.getHealth();
        if (curHealth < health) {
            wasHurt = true;
        } else {
            wasHurt = false;
        }
    }

    public void calculation() {

    }

    public void action() {
        if (wasHurt && curHealth < 100) {
            retreat();
        }
    }

    private void retreat() {
        try {

        } catch (Exception e) {
            System.out.println("error in retreat " + e);
        }

    }

    public void panicAlert(MapLocation m) {

    }

}
