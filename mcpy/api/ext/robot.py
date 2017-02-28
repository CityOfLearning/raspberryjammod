from ..core import minecraft as minecraft
from ..core import block as block
from ..core import facing as facing
from ..core.vec3 import Vec3
from ..core.util import flatten, floorFlatten
import time

class Robot:

    def __init__(self):
        self.mc = minecraft.Minecraft()
        self.robotId = self.mc.conn.sendReceive("robot.id")
        self.delayTime = 0.1

    def __str__(self):
        return self.mc.conn.sendReceive("robot.name", self.robotId)
        
    def robot(self):
        """Initialize the Robot"""
        self.robotId = self.mc.conn.sendReceive("robot.id")

    def inspect(self, *args):
        """Get block with data (x,y,z) => Block"""
        if len(args) > 0:
            newArgs = [int(self.robotId)]
            for arg in args:
                if type(arg) is int:
                    newArgs.append(arg)
                else:
                    raise TypeError(str(arg) + " is not a valid input")
            ans = self.mc.conn.sendReceive_flat("robot.inspect", floorFlatten(newArgs))
        else:
            ans = self.mc.conn.sendReceive("robot.inspect", self.robotId)
        return block.Block(*[int(x) for x in ans.split(",")[:2]])

    def turn(self, angle):
        """Compass turn of robot (turn:float/int) in degrees: 0=south, 90=west, 180=north, 270=west"""
        if type(angle) is int:
            self.mc.conn.send("robot.turn", self.robotId, angle)
            self.delay()
        else:
            raise TypeError(str(angle) + " is not a valid input")

    def delay(self):
        if self.delayTime > 0:
            time.sleep(self.delayTime)

     def detect(self):
        """Detect entities within a range of the robot"""
        ans = self.conn.sendReceive("robot.detect", self.robotId)
        entities = []
        for x in ans.split("%"):
            val = x.split("|")
            entities.append(Entity(val[0], val[1])
        return entities
        
    def attack(self, enemyId):
        """Attack Entity of the respective ID"""
        ans = self.conn.sendReceive("robot.attack", self.robotId, enemyId)
            
    def left(self):
        """Turn counterclockwise relative to compass heading"""
        self.turn(-90)

    def right(self):
        """Turn clockwise relative to compass heading"""
        self.turn(90)
        
    def setFacing(self, dir):
        if type(dir) is facing.Facing:
            self.mc.conn.send("robot.face", self.robotId, dir.id)
        elif type(dir) is int:
            self.mc.conn.send("robot.face", self.robotId, facing.fromId(dir).id)
        elif type(dir) is str:
            if len(dir) == 1:
                self.mc.conn.send("robot.face", self.robotId, facing.fromLetter(dir).id)
            else:
                self.mc.conn.send("robot.face", self.robotId, facing.fromName(dir).id)
        else:
            raise TypeError(str(dir) + " is not a valid input")
        self.delay()

    def forward(self, distance=1):
        """Move robot forward (distance: float)"""
        if type(distance) is int:
            self.mc.conn.sendReceive("robot.forward", self.robotId, distance)
        else:
            raise TypeError(str(distance) + " is not a valid input")

    def climb(self, distance=1):
        """Move robot climb (distance: float)"""
        if type(distance) is int:
            self.mc.conn.sendReceive("robot.climb", self.robotId, distance)
        else:
            raise TypeError(str(distance) + " is not a valid input")
        
    def descend(self, distance=1):
        """Move robot climb (distance: float)"""
        if type(distance) is int:
            self.mc.conn.sendReceive("robot.descend", self.robotId, distance)
        else:
            raise TypeError(str(distance) + " is not a valid input")

    def placeBlock(self, *args):
        """Place block in front of robot, else within 1x1x1 range of robot (x,y,z), robot uses inventory"""
        if len(args) > 0:
            newArgs = [int(self.robotId)]
            for arg in args:
                if type(arg) is int:
                    newArgs.append(arg)
                else:
                    raise TypeError(str(arg) + " is not a valid input")
            self.mc.conn.sendReceive_flat("robot.place", floorFlatten(newArgs))
        else:
            self.mc.conn.sendReceive("robot.place", self.robotId)
        self.delay()

    def breakBlock(self, *args):
        """Breaks block in front of robot, else within 1x1x1 range of robot (x,y,z)"""
        if len(args) > 0:
            newArgs = [int(self.robotId)]
            for arg in args:
                if type(arg) is int:
                    newArgs.append(arg)
                else:
                    raise TypeError(str(arg) + " is not a valid input")
            self.mc.conn.sendReceive_flat("robot.break", floorFlatten(newArgs))
        else:
            self.mc.conn.sendReceive("robot.break", self.robotId)
        self.delay()

    def backward(self, distance=1):
        """Move robot backwards, will change heading"""
        if type(distance) is int:
            self.mc.conn.sendReceive("robot.backward", self.robotId, distance)
        
    def say(self, phrase):
        """Have the robot speak"""
        self.mc.conn.send("robot.say", self.robotId, phrase.__str__())
        self.delay()
        
    def jump(self):
        """Make robot jump"""
        self.mc.conn.sendReceive("robot.jump", self.robotId)
        
    def interact(self, *args): 
        """Have the robot interact with the environment"""
        if len(args) > 0:
            newArgs = [int(self.robotId)]
            for arg in args:
                if type(arg) is int:
                    newArgs.append(arg)
                else:
                    raise TypeError(str(arg) + " is not a valid input")
            self.mc.conn.sendReceive_flat("robot.interact", floorFlatten(newArgs))
        else:
            self.mc.conn.send("robot.interact", self.robotId)
        self.delay()
    
    def getDirection(self):
        """Get entity direction (entityId:int) => Vec3"""
        s = self.mc.conn.sendReceive("robot.getDirection", self.robotId)
        return Vec3((float(x) for x in s.split(",")))

    def getPos(self):
        """Get entity position (entityId:int) => Vec3"""
        s = self.mc.conn.sendReceive("robot.getPos", self.robotId)
        return Vec3((float(x) for x in s.split(",")))
        
    def getRotation(self):
        """Get entity direction (entityId:int) => Vec3"""
        s = self.mc.conn.sendReceive("robot.getRotation", self.robotId)
        return float(s)
