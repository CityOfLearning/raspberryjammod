#
# Code by Alexander Pruss and under the MIT license
#

import mcpi.minecraft as minecraft
import mcpi.block as block
from mcpi.entity import *
import numbers
import copy
import time
from drawing import *
from operator import itemgetter
from math import *
import numbers

Block = block.Block

class Robot:

    def __init__(self,mc=None):
        if mc:
             self.mc = mc
        else:
             self.mc = minecraft.Minecraft()
        self.block = block.DIRT
        self.directionIn()
        self.positionIn()
        self.robotId = minecraft.conn.send("robot.id")
        self.setEntityCommands()
        self.delayTime = 0.05

    def setEntityCommands(self):
            self.setPos = lambda *pos: self.mc.conn.send("robot.setPos",self.robotId,*pos)
            self.setRotation = lambda angle: self.mc.conn.send("robot.setRotation", self.robotId,angle)

    def robot(self):
        """Initialize the Robot"""
        self.robotId = minecraft.conn.send("robot.id")
        self.setEntityCommands()
        self.gridalign()
        self.positionOut()
        self.directionOut()

    def goto(self,x,y,z):
        """Teleport robot to location (x:int, y:int, z:int)"""
        self.position.x = x
        self.position.y = y
        self.position.z = z
        self.positionOut()
        self.delay()

    def inspect(self):
        """Get block with data (x,y,z) => Block"""
        ans = self.conn.sendReceive_flat("robot.inspect", floorFlatten(args))
        return Block(*[int(x) for x in ans.split(",")[:2]])

    def angle(self,angle):
        """Compass angle of robot (angle:float/int) in degrees: 0=south, 90=west, 180=north, 270=west"""
        angles = self.getMinecraftAngles()
        self.matrix = matrixMultiply(yawMatrix(angle), pitchMatrix(angles[1]))
        self.directionOut()

    def positionIn(self):
        pos = minecraft.conn.send("robot.getPos")
        self.position = minecraft.Vec3(int(round(pos.x)),int(round(pos.y)),int(round(pos.z)))

    def positionOut(self):
        self.setPos(self.position)

    def delay(self):
        if self.delayTime > 0:
            time.sleep(self.delayTime)

    def directionIn(self):
        rotation = minecraft.conn.send("robot.getRotation")
        self.matrix = matrixMultiply(yawMatrix(rotation), pitchMatrix(0))

    def getHeading(self):
        return [self.matrix[0][2],self.matrix[1][2],self.matrix[2][2]]

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
        self.right(-angle)

    def right(self, angle=90):
        """Turn clockwise relative to compass heading"""
        self.matrix = matrixMultiply(yawMatrix(angle), self.matrix)
        self.directionOut()
        self.delay()

    def forward(self, distance=1):
        """Move robot forward (distance: float)"""
#        pitch = self.pitch * pi/180.
#        rot = self.rotation * pi/180.
        # at pitch=0: rot=0 -> [0,0,1], rot=90 -> [-1,0,0]
#        dx = cos(-pitch) * sin(-rot)
#        dy = sin(-pitch)
#        dz = cos(-pitch) * cos(-rot)
        [dx,dy,dz] = self.getHeading()
        newX = self.position.x + dx * distance
        newY = self.position.y + dy * distance
        newZ = self.position.z + dz * distance
        self.position.x = newX
        self.position.y = newY
        self.position.z = newZ
        self.positionOut()
        self.delay()

    def placeBlock(self, *args):
        """Set block (id,[data]), can be empty so robot uses inventory"""
        self.conn.send_flat("robot.place", floorFlatten(args))

    def breakBlock(self, *args):
        """Breaks block in front of robot, else within 1x1x1 range of robot (x,y,z)"""
        self.conn.send_flat("robot.break", floorFlatten(args))

    def back(self, distance=1):
        """Move robot backwards and keeping heading unchanged"""
#        pitch = self.pitch * pi/180.
#        rot = self.rotation * pi/180.
#        dx = - cos(-pitch) * sin(-rot)
#        dy = - sin(-pitch)
#        dz = - cos(-pitch) * cos(-rot)
        [dx,dy,dz] = self.getHeading()
        newX = self.position.x - dx * distance
        newY = self.position.y - dy * distance
        newZ = self.position.z - dz * distance
        self.position.x = newX
        self.position.y = newY
        self.position.z = newZ
        self.positionOut()
        self.delay()

    def gridalign(self):
        """Align positions to grid"""
        self.position.x = int(round(self.position.x))
        self.position.y = int(round(self.position.y))
        self.position.z = int(round(self.position.z))

        bestDist = 2 * 9
        bestMatrix = makeMatrix(0,0,0)

        for compass in [0, 90, 180, 270]:
            for pitch in [0, 90, 180, 270]:
                for roll in [0, 90, 180, 270]:
                    m = makeMatrix(compass,pitch,roll)
                    dist = matrixDistanceSquared(self.matrix, m)
                    if dist < bestDist:
                        bestMatrix = m
                        bestDist = dist

        self.matrix = bestMatrix
        self.positionOut()
        self.directionOut()