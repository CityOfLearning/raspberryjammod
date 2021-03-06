from __future__ import print_function
#
# WARNING: If you're running RJM on a server, do NOT include this script server-side for security reasons.
#

#
# Code by Alexander Pruss and under the MIT license
#
# This script only works on Raspberry Jam
#

from api.core import minecraft as minecraft
from api.core.vec3 import Vec3
from api.core import block as block
import time
from math import *
import code
import sys


def quit():
    sys.exit()

def inputLine(prompt):
    mc.events.clearAll()
    while True:
        chats = mc.events.pollChatPosts()
        for c in chats:
            if c.entityId == playerId:
                print(c.message)
                if c.message == 'quit':
                    return 'quit()'
                elif c.message == ' ':
                    return ''
                elif "__" in c.message:
                    sys.exit();
                else:
                    return c.message
        time.sleep(0.2)

mc = minecraft.Minecraft()
playerPos = mc.player.getPos()
playerId = mc.getPlayerId()

mc.postToChat("Enter python code into chat, type 'quit' to quit.")
i = code.interact(banner="Minecraft Python ready", readfunc=inputLine, local=locals())
