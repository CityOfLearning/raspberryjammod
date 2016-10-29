#
# Code by Alexander Pruss and under the MIT license
#

import mcpi.minecraft as minecraft
import mcpi.block as block
from mcpi.vec3 import Vec3
from mcpi.util import flatten, floorFlatten
import time

Block = block.Block

class Robot:

    def __init__(self):
        self.mc = minecraft.Minecraft()
        self.robotId = self.mc.conn.sendReceive("robot.id")
        self.delayTime = 0.05

    def robot(self):
        """Initialize the Robot"""
        self.robotId = self.mc.conn.sendReceive("robot.id")

    def inspect(self, *args):
        """Get block with data (x,y,z) => Block"""
        ans = self.mc.conn.sendReceive_flat("robot.inspect", self.robotId, floorFlatten(args))
        return Block(*[int(x) for x in ans.split(",")[:2]])

    def turn(self, angle):
        """Compass turn of robot (turn:float/int) in degrees: 0=south, 90=west, 180=north, 270=west"""
        self.mc.conn.send("robot.turn", self.robotId, angle)
        self.delay()

    def delay(self):
        if self.delayTime > 0:
            time.sleep(self.delayTime)

    def left(self):
        """Turn counterclockwise relative to compass heading"""
        self.turn(-90)

    def right(self):
        """Turn clockwise relative to compass heading"""
        self.turn(90)

    def forward(self, distance=1):
        """Move robot forward (distance: float)"""
        self.mc.conn.send("robot.forward", self.robotId, distance)

    def placeBlock(self, *args):
        """Place block (id,[data]), can be empty so robot uses inventory"""
        if len(args) > 0:
            self.mc.conn.send_flat("robot.place", self.robotId, floorFlatten(*args))
        else:
            self.mc.conn.send("robot.place", self.robotId)

    def breakBlock(self, *args):
        """Breaks block in front of robot, else within 1x1x1 range of robot (x,y,z)"""
        if len(args) > 0:
            self.mc.conn.send_flat("robot.break", self.robotId, floorFlatten(*args))
        else:
            self.mc.conn.send("robot.break", self.robotId)

    def backward(self, distance=1):
        """Move robot backwards, will change heading"""
        self.mc.conn.send("robot.backward", self.robotId, distance)
        
    def jump(self):
        """Make robot jump"""
        self.mc.conn.send("robot.jump", self.robotId)
        
    def getDirection(self):
        """Get entity direction (entityId:int) => Vec3"""
        s = self.mc.conn.sendReceive("robot.getDirection", self.robotId)
        return Vec3((float(x) for x in s.split(",")))

    def getPos(self):
        """Get entity position (entityId:int) => Vec3"""
        s = self.mc.conn.sendReceive("robot.getPos", self.robotId)
        return Vec3((float(x) for x in s.split(",")))
