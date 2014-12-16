package main.java.agents;

import edu.wlu.cs.levy.CG.KeySizeException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import javafx.geometry.Point2D;
import main.java.utils.AgentInTree;

import java.util.ArrayList;

/**
 * Created by Jakub Fortunka on 20.11.14.
 *
 */
public abstract class AgentWithPosition extends Agent {
    protected int fieldOfView = 200;

    protected double previousRatio=1;

    protected double psychologicalResistance = 0.7;

    protected World world;

    protected AgentInTree position;

    protected abstract boolean enemyInRangeOfAttack(AgentInTree enemy);

    protected abstract AgentInTree getNearestEnemy();

    protected abstract void gotoEnemy(AgentInTree enemy);

    //protected abstract void keepPosition();

    public AgentInTree getPosition() {
        return position;
    }

    public ArrayList<AID> getMinionsWithinRange(Commander agent) {
        ArrayList<AgentInTree> list = new ArrayList<>();
        try {
            world.getAgentsTree()
                    .nearestEuclidean(new double[]{agent.position.p.getX(), agent.position.p.getY()},agent.attractionForce)
                    .stream()
                    .filter(a -> a.side==agent.position.side)
                    .forEach(list::add);
        } catch (KeySizeException e) {
            e.printStackTrace();
        }
        if(list.contains(agent.position))
            list.remove(agent.position);
        ArrayList<AID> ans = new ArrayList<>();
        for (AgentInTree a :
                list) {
            ans.add(new AID(a.getAgentName(),true));
        }
        return ans;
    }

    public abstract void reactToAttack(ACLMessage msg);

    public boolean isMotivated() {
        int [] count;
        count = world.countFriendFoe(this);
        //System.out.println("Friends: " + count[0] + " Enemies: " + count[1]);
        if (count[1] == 0) {
            position.morale += 4;
            return position.morale > 0;
        } else if (count[0] == 0) {
            position.morale -= 4;
            return position.morale > 0;
        }
        double ratio = ((double)count[0])/((double)count[1]);
        //System.out.println("Ratio: " + ratio);
        if (ratio < psychologicalResistance && ratio < previousRatio)
            position.morale -= (1/ratio +2);
        if (ratio >= 1 && position.morale<50)
            position.morale += ratio;
        previousRatio = ratio;
        //System.out.println(getLocalName() + " Morale: " + morale);
        return position.morale > 0;
    }

    protected abstract void killYourself(ACLMessage msgToSend);

    public double[] getSpeedHV() {
        double angle = position.speed[0], r = position.speed[1];
        return new double[]{r * Math.cos(angle), r * Math.sin(angle)};
    }

    public void setSpeedHV(double hSpeed, double vSpeed) {
        setSpeedVector(Math.atan2(vSpeed, hSpeed), Math.sqrt(hSpeed*hSpeed + vSpeed*vSpeed));
    }

    public void setSpeedHV(double hSpeed, double vSpeed, double limit) {
        setSpeedVector(Math.atan2(vSpeed, hSpeed), Math.sqrt(hSpeed*hSpeed + vSpeed*vSpeed) - limit);
    }

    public void setSpeedVector(double angle, double radius) {
        position.speed[0] = angle;
        position.speed[1] = radius;
    }

    protected Point2D gesDestination() {
        Point2D pos = position.pos();
        double[] s = getSpeedHV();
        return new Point2D(pos.getX() + s[0], pos.getY() + s[1]);
    }

}
