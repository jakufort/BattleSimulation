package main.java.agents;

import jade.core.AID;
import javafx.geometry.Point2D;
import main.java.utils.AgentInTree;

import java.util.List;

/**
 * Created by Jakub Fortunka on 18.11.14.
 *
 */
public abstract class CannonFodder extends AgentWithPosition {

    protected int condition, strength, speed, accuracy;

    public void setup() {
        // 0 - behaviour
        // 1 - condition
        // 2 - strength
        // 3 - speed
        // 4 - accuracy
        // 5 - world
        // 6 - position

        Object[] parameters = getArguments();

        addBehaviour((ReactiveBehaviour) parameters[0]);
        this.condition = (int) parameters[1];
        this.strength = (int) parameters[2];
        this.speed = (int) parameters[3];
        this.accuracy = (int) parameters[4];
        this.world = (World) parameters[5];
        this.position = (AgentInTree) parameters[6];
    }

    @Override
    public void doDelete() {
        world.removeAgent(this);
        super.doDelete();
    }

    @Override
    protected AgentInTree getNearestEnemy() {
        return world.getNearestEnemy(this);
    }

    @Override
    protected void gotoEnemy(AgentInTree enemy) {
        Point2D mp = position.pos();
        Point2D ep = enemy.pos();
        setSpeedHV(ep.getX() - mp.getX(), ep.getY() - mp.getY());

        world.moveAgent(this, gesDestination());
    }

    protected void keepPosition() {
        //List<KdTree.Placed> friendlyNeighbors;
        List<AgentInTree> friendlyNeighbors;

        friendlyNeighbors = world.getNeighborFriends(this);

        Point2D thisPosition = position.pos();
        double vec[] = {0, 0};
        double posX, posY, srDistance = 0, pomDistance;
        for (AgentInTree friendlyNeighbor : friendlyNeighbors) {
            posX = friendlyNeighbor.pos().getX();
            posY = friendlyNeighbor.pos().getY();
            srDistance = srDistance + Math.sqrt(Math.pow(posX - thisPosition.getX(), 2) + Math.pow(posY - thisPosition.getY(), 2));
        }
        for (AgentInTree friendlyNeighbor : friendlyNeighbors) {
            posX = friendlyNeighbor.pos().getX();
            posY = friendlyNeighbor.pos().getY();
            pomDistance = Math.sqrt(Math.pow(posX - thisPosition.getX(), 2) + Math.pow(posY - thisPosition.getY(), 2));
            vec[0] = vec[0] + ((posX - thisPosition.getX()) * (pomDistance - srDistance)) / pomDistance;
            vec[1] = vec[1] + ((posY - thisPosition.getY()) * (pomDistance - srDistance)) / pomDistance;
        }
        vec[0] = Math.round(vec[0]);
        vec[1] = Math.round(vec[1]);
        if (vec[0] != 0) vec[0] = vec[0]/Math.abs(vec[0]);
        if (vec[1] != 0) vec[1] = vec[1]/Math.abs(vec[1]);
        // Just move your ass...
        if(vec[0] == 0.0 && vec[1] == 0.0){
            vec[0] = world.computeBoardCenter(this.position.pos());
        }
        Point2D destination = new Point2D(thisPosition.getX() + vec[0], thisPosition.getY() + vec[1]);
        world.moveAgent(this,destination);
    }

    protected abstract void attack(AID enemy, AgentInTree position);

    @Override
    public void setSpeedVector(double angle, double radius) {
        super.setSpeedVector(angle, Math.min(radius, speed));
    }
}
