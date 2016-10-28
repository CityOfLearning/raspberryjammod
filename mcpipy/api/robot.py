#
# Code by Alexander Pruss and under the MIT license
#

import mcpi.minecraft as minecraft
import time

Block = block.Block

class Robot:

    def __init__(self,mc=None):
        if mc:
             self.mc = mc
        else:
             self.mc = minecraft.Minecraft()
        self.block = block.DIRT
        self.robotId = minecraft.conn.send("robot.id")
        self.delayTime = 0.05

    def robot(self):
        """Initialize the Robot"""
        self.robotId = minecraft.conn.send("robot.id")

    def inspect(self, *args):
        """Get block with data (x,y,z) => Block"""
        ans = self.conn.sendReceive_flat("robot.inspect", floorFlatten(args))
        return Block(*[int(x) for x in ans.split(",")[:2]])

    def angle(self,angle):
        """Compass angle of robot (angle:float/int) in degrees: 0=south, 90=west, 180=north, 270=west"""
        self.conn.sendReceive_flat("robot.turn", floorFlatten(angle))
		self.delay()

    def positionIn(self):
        pos = minecraft.conn.send("robot.getPos")
        self.position = minecraft.Vec3(int(round(pos.x)),int(round(pos.y)),int(round(pos.z)))

    def positionOut(self):
        self.setPos(self.position)

    def delay(self):
        if self.delayTime > 0:
            time.sleep(self.delayTime)

    def getMinecraftAngles(self):
        heading = self.getHeading()

        if isinstance(heading[0], numbers.Integral) and isinstance(heading[1], numbers.Integral) and isinstance(heading[2], numbers.Integral):
            # the only way all coordinates of the heading can be integers is if
            # we are
            # grid aligned

            # no need for square roots; could also use absolute value
            xz = abs(heading[0]) + abs(heading[2])
            if xz != 0:
                rotation = iatan2(-heading[0], heading[2])
            else:
                rotation = 0
            pitch = iatan2(-heading[1], xz)
        else:        
            xz = sqrt(heading[0] * heading[0] + heading[2] * heading[2])
            if xz >= 1e-9:
                rotation = atan2(-heading[0], heading[2]) * TO_DEGREES
            else:
                rotation = 0.
            pitch = atan2(-heading[1], xz) * TO_DEGREES
        return [rotation,pitch]

    def directionOut(self):
        heading = self.getHeading()
        xz = sqrt(heading[0] * heading[0] + heading[2] * heading[2])
        pitch = atan2(-heading[1], xz) * TO_DEGREES
        self.setPitch(pitch)
        if xz >= 1e-9:
            rotation = atan2(-heading[0], heading[2]) * TO_DEGREES
            self.setRotation(rotation)

    def left(self, angle=-90):
        """Turn counterclockwise relative to compass heading"""
        

    def right(self, angle=90):
        """Turn clockwise relative to compass heading"""
		

    def forward(self, distance=1):
        """Move robot forward (distance: float)"""

    def placeBlock(self, *args):
        """Set block (id,[data]), can be empty so robot uses inventory"""
        self.conn.send_flat("robot.place", floorFlatten(args))

    def breakBlock(self, *args):
        """Breaks block in front of robot, else within 1x1x1 range of robot (x,y,z)"""
        self.conn.send_flat("robot.break", floorFlatten(args))

    def back(self, distance=1):
        """Move robot backwards and keeping heading unchanged"""
		
        
	def getDirection(self):
        """Get entity direction (entityId:int) => Vec3"""
        s = self.conn.sendReceive("robot.getDirection", self.robotId)
        return Vec3((float(x) for x in s.split(",")))

    def getPos(self):
        """Get entity position (entityId:int) => Vec3"""
        s = self.conn.sendReceive("robot.getPos", self.robotId)
        return Vec3((float(x) for x in s.split(",")))