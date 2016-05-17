
## Get the torso. 
from poppy.creatures import PoppyTorso
from threading import Thread
from pypot.server.httpserver import HTTPRobotServer

torso = PoppyTorso()

server = HTTPRobotServer(torso, host='193.50.110.242', port=8080)

Thread(target=lambda: server.run(quiet=True, server='wsgiref')).start()
